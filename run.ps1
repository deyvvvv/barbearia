# Comando unico para a apresentacao.
#   .\run        -> abre o sistema (menu interativo)
#   .\run -Demo  -> roda a demonstracao automatica com banco, testes e exemplos
param([switch]$Demo)

$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $MyInvocation.MyCommand.Path

if ($Demo) {
    Write-Host "Rodando demonstracao automatica..." -ForegroundColor Cyan
    & powershell -NoProfile -ExecutionPolicy Bypass -File "$project\scripts\smoke.ps1"
    if ($LASTEXITCODE -ne 0) { throw "A demonstracao falhou." }
} else {
    & powershell -NoProfile -ExecutionPolicy Bypass -File "$project\executar.ps1"
}
