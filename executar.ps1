# Inicia o PostgreSQL (se necessario) e abre o menu da Barbearia do Braz.
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new()

$project = Split-Path -Parent $MyInvocation.MyCommand.Path
$tools = "$env:USERPROFILE\AppData\Local\Temp\opencode\tools"

. "$project\scripts\ambiente.ps1"

$javaCmd = Resolve-JavaCmd -Ferramentas $tools
$psqlCmd = Resolve-PsqlCmd -Ferramentas $tools
$pgBin = Split-Path -Parent $psqlCmd
$pgData = if ($env:PGDATA) { $env:PGDATA } else { "$tools\pgdata" }

Start-PostgresSeNecessario -PgBin $pgBin -PgData $pgData -LogFile "$tools\pg.log" | Out-Null

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   INICIANDO BARBEARIA DO BRAZ" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

& $javaCmd "-Dfile.encoding=UTF-8" -cp "$project\build;$project\lib\*" Main

Read-Host "`nPressione Enter para sair"
