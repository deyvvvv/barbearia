# Smoke test: recria o banco, compila e percorre as opcoes novas do menu.
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()

$project = Split-Path -Parent $PSScriptRoot
$tools = "$env:USERPROFILE\AppData\Local\Temp\opencode\tools"

. "$PSScriptRoot\ambiente.ps1"

$javaCmd = Resolve-JavaCmd -Ferramentas $tools
$javacCmd = Join-Path (Split-Path -Parent $javaCmd) 'javac.exe'
$psqlCmd = Resolve-PsqlCmd -Ferramentas $tools
$pgBin = Split-Path -Parent $psqlCmd
$pgData = if ($env:PGDATA) { $env:PGDATA } else { "$tools\pgdata" }

function Invoke-Psql([string[]]$argumentos) {
    $anterior = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    & $psqlCmd @argumentos 2>$null | Out-Null
    $codigo = $LASTEXITCODE
    $ErrorActionPreference = $anterior
    if ($codigo -ne 0) { throw "psql retornou $codigo para: $($argumentos -join ' ')" }
}

Start-PostgresSeNecessario -PgBin $pgBin -PgData $pgData -LogFile "$tools\pg.log" -Silencioso | Out-Null

Invoke-Psql @('-U', 'postgres', '-d', 'postgres', '-c',
    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'barbearia' AND pid <> pg_backend_pid();")
Invoke-Psql @('-U', 'postgres', '-d', 'postgres', '-c', 'DROP DATABASE IF EXISTS barbearia;')
Invoke-Psql @('-U', 'postgres', '-d', 'postgres', '-f', "$project\sql\barbearia.sql")
Invoke-Psql @('-U', 'postgres', '-d', 'postgres', '-f', "$project\sql\testes.sql")

& $javacCmd -encoding UTF-8 -cp "$project\lib\*" -d "$project\build" `
    (Get-ChildItem "$project\src" -Recurse -Filter *.java).FullName
if ($LASTEXITCODE -ne 0) { throw "Falha na compilacao" }

$entrada = @('9', '1', '2', 'PIX', '0', '9', '2', '1', '0',
             '9', '3', '1', '0', '9', '4', '0', '10', '0')
$anterior = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
$saida = ($entrada | & $javaCmd "-Dfile.encoding=UTF-8" -cp "$project\build;$project\lib\*" Main 2>&1 | Out-String)
$ErrorActionPreference = $anterior

$esperados = @(
    'ATENDIMENTO FINALIZADO',
    'FATURAMENTO POR BARBEIRO',
    'PRECO DO SERVICO',
    'AUDITORIA DE PAGAMENTOS',
    'COMANDOS PARA O SEMINARIO',
    '.\executar.ps1'
)

$faltando = @()
foreach ($texto in $esperados) {
    if ($saida -notlike "*$texto*") { $faltando += $texto }
}

if ($faltando.Count -gt 0) {
    Write-Output $saida
    throw "Smoke falhou; texto ausente: $($faltando -join ', ')"
}

Write-Output 'PASS: menu e objetos SQL funcionando'
