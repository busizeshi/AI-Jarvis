# Windows 版本 proto 生成脚本（PowerShell）
# 在 note-gather-ai/ 目录下执行：.\scripts\gen_proto.ps1
$ErrorActionPreference = "Stop"

$ProtoSrc = (Resolve-Path "$PSScriptRoot\..\..\protos").Path
$OutDir   = (Resolve-Path "$PSScriptRoot\..\shared\pb").Path

Write-Host "PROTO_SRC=$ProtoSrc"
Write-Host "OUT_DIR=$OutDir"

python -m grpc_tools.protoc `
  -I "$ProtoSrc" `
  --python_out="$OutDir" `
  --grpc_python_out="$OutDir" `
  "$ProtoSrc\ng_ai.proto"

# 修正 grpc stub 中的相对 import
$grpcFile = Join-Path $OutDir "ng_ai_pb2_grpc.py"
if (Test-Path $grpcFile) {
    (Get-Content $grpcFile) -replace '^import ng_ai_pb2', 'from shared.pb import ng_ai_pb2' |
        Set-Content $grpcFile
}

Write-Host "gRPC 存根生成完成 -> $OutDir"
