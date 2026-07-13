#!/usr/bin/env bash
# scripts/gen_proto.sh
# 生成 gRPC Python 存根（ng_ai_pb2.py / ng_ai_pb2_grpc.py）
# 运行环境：在 note-gather-ai/ 目录下执行
# 前提：poetry install 已完成（grpcio-tools 已安装）
set -e

PROTO_SRC="$(cd "$(dirname "$0")/../.." && pwd)/protos"
OUT_DIR="$(cd "$(dirname "$0")/.." && pwd)/shared/pb"

echo "PROTO_SRC=${PROTO_SRC}"
echo "OUT_DIR=${OUT_DIR}"

python -m grpc_tools.protoc \
  -I "${PROTO_SRC}" \
  --python_out="${OUT_DIR}" \
  --grpc_python_out="${OUT_DIR}" \
  "${PROTO_SRC}/ng_ai.proto"

# 修正相对导入（grpc_tools 生成的 import 需要加包前缀）
sed -i 's/^import ng_ai_pb2/from shared.pb import ng_ai_pb2/' \
  "${OUT_DIR}/ng_ai_pb2_grpc.py" 2>/dev/null || true

echo "gRPC 存根生成完成 -> ${OUT_DIR}"
