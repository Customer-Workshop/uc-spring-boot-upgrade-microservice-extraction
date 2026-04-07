### CPU + メモリ負荷テスト（30分後に自動停止）###

$durationMinutes = 30
$endTime = (Get-Date).AddMinutes($durationMinutes)

Write-Host "CPU とメモリの負荷テストを $durationMinutes 分間実行します..."
Write-Host "終了予定時刻: $endTime"
Start-Sleep -Seconds 2

########################
# CPU ストレス
# 1コアをメモリストレス用に残し、残りのコアでCPU負荷をかける
########################
Write-Host "CPU ストレスジョブを開始します..."
$cpuCores = [Math]::Max(1, [int]$env:NUMBER_OF_PROCESSORS - 1)
for ($i = 0; $i -lt $cpuCores; $i++) {
    Start-Job -Name "CPUStress_$i" -ScriptBlock {
        param($end)
        $endTime = [DateTime]$end
        while ((Get-Date) -lt $endTime) {
            # CPU負荷ループ（指定時間まで）
        }
    } -ArgumentList $endTime.ToString("o")
}

########################
# メモリ ストレス
# 独立したバックグラウンドジョブとして実行し、CPU負荷の影響を受けないようにする
########################
Write-Host "メモリストレスジョブを開始します..."
$memoryJob = Start-Job -Name "MemoryStress" -ScriptBlock {
    param($end)
    $endTime = [DateTime]$end
    $buffers = @()

    while ((Get-Date) -lt $endTime) {
        # 200MB を繰り返し確保
        $buffers += , (New-Object byte[] ([int](200MB)))
        Start-Sleep -Milliseconds 200
    }

    # ジョブ内でメモリを解放
    Remove-Variable buffers -ErrorAction SilentlyContinue
    [System.GC]::Collect()
} -ArgumentList $endTime.ToString("o")

########################
# 指定時間まで待機
########################
Write-Host "負荷テスト実行中... 終了時刻まで待機します。"
while ((Get-Date) -lt $endTime) {
    Start-Sleep -Seconds 10
}

Write-Host "指定時間に達しました。負荷テストを停止します..."

########################
# CPU クリーンアップ
########################
Get-Job -Name "CPUStress_*" | Stop-Job -Force
Get-Job -Name "CPUStress_*" | Remove-Job -Force

########################
# メモリ クリーンアップ
########################
Get-Job -Name "MemoryStress" | Stop-Job -Force
Get-Job -Name "MemoryStress" | Remove-Job -Force
[System.GC]::Collect()

Write-Host "負荷テストが完了し、すべてのリソースを解放しました。"
