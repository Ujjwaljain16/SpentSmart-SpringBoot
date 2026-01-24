# PowerShell script to run docker-compose with .env variables
$envFile = ".env"

if (Test-Path $envFile) {
    Write-Host "Loading .env file..."
    Get-Content $envFile | ForEach-Object {
        if ($_ -match "^\s*([^#=]+)\s*=\s*(.*)$") {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            [System.Environment]::SetEnvironmentVariable($name, $value, [System.EnvironmentVariableTarget]::Process)
        }
    }
} else {
    Write-Warning ".env file not found! Docker Compose might fail if it relies on these variables."
}

# Stop existing containers to avoid conflicts
docker-compose down

# Build and start
docker-compose up --build -d

Write-Host "Docker containers started. Check status with: docker-compose ps"
