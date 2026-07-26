#!/usr/bin/env bash
set -eo pipefail

# Script to local-load kubectl and argocd binaries into project environment
BIN_DIR="./bin"
mkdir -p "$BIN_DIR"

echo "Installing kubectl..."
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
mv kubectl "$BIN_DIR/kubectl"

echo "Installing argocd CLI..."
curl -sSL -o argocd-linux-amd64 https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
chmod +x argocd-linux-amd64
mv argocd-linux-amd64 "$BIN_DIR/argocd"

echo "Kubectl and ArgoCD binaries installed successfully in '$BIN_DIR'."
export PATH="$BIN_DIR:$PATH"
echo "To use them, run: export PATH=\"\$PWD/bin:\$PATH\""
