# Funcoes compartilhadas para localizar Java/PostgreSQL e subir o banco.
# Preferencia: variaveis de ambiente (JAVA_HOME, PGBIN, PGDATA), depois PATH,
# e por fim a instalacao portatil em AppData\Local\Temp\opencode\tools.

function Resolve-JavaCmd {
    param([string]$Ferramentas)

    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
        return "$env:JAVA_HOME\bin\java.exe"
    }

    $comando = Get-Command java -ErrorAction SilentlyContinue
    if ($comando) { return $comando.Source }

    $arquivo = Get-ChildItem "$Ferramentas\jdk21" -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $arquivo) {
        throw 'Java nao encontrado. Instale o JDK 21, ajuste o PATH ou defina JAVA_HOME.'
    }
    return $arquivo
}

function Resolve-PsqlCmd {
    param([string]$Ferramentas)

    if ($env:PGBIN -and (Test-Path "$env:PGBIN\psql.exe")) {
        return "$env:PGBIN\psql.exe"
    }

    $comando = Get-Command psql -ErrorAction SilentlyContinue
    if ($comando) { return $comando.Source }

    $arquivo = Get-ChildItem "$Ferramentas\pg16" -Recurse -Filter psql.exe -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $arquivo) {
        throw 'psql nao encontrado. Instale o PostgreSQL, ajuste o PATH ou defina PGBIN.'
    }
    return $arquivo
}

function Start-PostgresSeNecessario {
    param(
        [string]$PgBin,
        [string]$PgData,
        [string]$LogFile,
        [int]$Espera = 30,
        [switch]$Silencioso
    )

    $isReady = Join-Path $PgBin 'pg_isready.exe'
    $pgCtl = Join-Path $PgBin 'pg_ctl.exe'

    $anterior = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    if (Test-Path $isReady) {
        & $isReady -h localhost -p 5432 2>$null | Out-Null
    }
    $codigo = $LASTEXITCODE
    $ErrorActionPreference = $anterior

    if ($codigo -eq 0) { return $true }

    if (-not (Test-Path $pgCtl) -or -not (Test-Path "$PgData\PG_VERSION")) {
        throw "PostgreSQL nao esta rodando e nao ha cluster configurado em '$PgData'. " +
              'Inicie o servico do PostgreSQL ou defina PGBIN e PGDATA.'
    }

    if (-not $Silencioso) { Write-Host 'Iniciando PostgreSQL...' -ForegroundColor Green }
    & $pgCtl -D $PgData -l $LogFile start | Out-Null

    $segundos = 0
    do {
        Start-Sleep -Seconds 1
        $segundos++
        $ErrorActionPreference = 'Continue'
        & $isReady -h localhost -p 5432 2>$null | Out-Null
        $codigo = $LASTEXITCODE
        $ErrorActionPreference = 'Stop'
    } while ($codigo -ne 0 -and $segundos -lt $Espera)

    if ($codigo -ne 0) {
        throw 'PostgreSQL nao ficou pronto a tempo. Verifique o log: ' + $LogFile
    }
    if (-not $Silencioso) { Write-Host 'PostgreSQL pronto!' -ForegroundColor Green }
    return $true
}
