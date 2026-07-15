param(
    [string]$NacosAddr = $(if ($env:NACOS_ADDR) { $env:NACOS_ADDR } else { "10.142.244.123:8848" }),
    [string]$NacosUsername = $(if ($env:NACOS_USERNAME) { $env:NACOS_USERNAME } else { "nacos" }),
    [string]$NacosPassword = $(if ($env:NACOS_PASSWORD) { $env:NACOS_PASSWORD } else { "nacos" }),
    [string]$Namespace = $(if ($env:NACOS_NAMESPACE) { $env:NACOS_NAMESPACE } else { "public" })
)

$ErrorActionPreference = "Stop"

function Join-NacosUrl {
    param([string]$Path)

    $normalizedAddr = $NacosAddr.Trim()
    if ($normalizedAddr -notmatch "^https?://") {
        $normalizedAddr = "http://$normalizedAddr"
    }

    $normalizedAddr = $normalizedAddr.TrimEnd("/")
    return "$normalizedAddr/nacos/$Path"
}

function Get-NacosAccessToken {
    $loginUrl = Join-NacosUrl "v1/auth/users/login"
    $response = Invoke-RestMethod `
        -Method Post `
        -Uri $loginUrl `
        -ContentType "application/x-www-form-urlencoded" `
        -Body @{
            username = $NacosUsername
            password = $NacosPassword
        }

    if (-not $response.accessToken) {
        throw "Nacos login succeeded but accessToken is empty."
    }

    return $response.accessToken
}

function Publish-NacosConfig {
    param(
        [string]$DataId,
        [string]$Group,
        [string]$FilePath,
        [string]$AccessToken
    )

    if (-not (Test-Path -LiteralPath $FilePath)) {
        throw "Config file not found: $FilePath"
    }

    $content = Get-Content -Raw -Encoding UTF8 -LiteralPath $FilePath
    $publishUrl = Join-NacosUrl "v1/cs/configs"
    $escapedToken = [System.Uri]::EscapeDataString($AccessToken)
    $requestUrl = "${publishUrl}?accessToken=$escapedToken"

    $body = @{
        dataId = $DataId
        group = $Group
        content = $content
        type = "yaml"
    }

    if ($Namespace -and $Namespace -ne "public") {
        $body.tenant = $Namespace
    }

    $result = Invoke-RestMethod `
        -Method Post `
        -Uri $requestUrl `
        -ContentType "application/x-www-form-urlencoded; charset=UTF-8" `
        -Body $body

    if ($result -ne $true -and $result -ne "true") {
        throw "Publish failed: dataId=$DataId group=$Group result=$result"
    }

    Write-Host "Published $Group/$DataId"
}

$configs = @(
    @{ DataId = "common.yaml"; Group = "COMMON_GROUP"; File = "common.yaml" },
    @{ DataId = "ng-gateway.yaml"; Group = "DEFAULT_GROUP"; File = "ng-gateway.yaml" },
    @{ DataId = "ng-admin.yaml"; Group = "DEFAULT_GROUP"; File = "ng-admin.yaml" },
    @{ DataId = "ng-biz.yaml"; Group = "DEFAULT_GROUP"; File = "ng-biz.yaml" }
)

$token = Get-NacosAccessToken

foreach ($config in $configs) {
    $filePath = Join-Path $PSScriptRoot $config.File
    Publish-NacosConfig `
        -DataId $config.DataId `
        -Group $config.Group `
        -FilePath $filePath `
        -AccessToken $token
}

Write-Host "All Nacos configs have been published."
