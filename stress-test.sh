#!/bin/bash

duration_minutes=30
duration_seconds=$((duration_minutes * 60))
end_time=$(($(date +%s) + duration_seconds))

echo "Starting CPU + Memory stress for $duration_minutes minutes..."
echo "End time: $(date -d @$end_time 2>/dev/null || date -r $end_time 2>/dev/null || echo "in $duration_minutes minutes")"
sleep 2

########################
# CPU Stress
# Reserve 1 core for memory stress, use the rest for CPU load
########################
cpu_cores=$(nproc)
cpu_stress_cores=$((cpu_cores - 1))
if [ "$cpu_stress_cores" -lt 1 ]; then
    cpu_stress_cores=1
fi

echo "Starting CPU stress on $cpu_stress_cores core(s)..."
cpu_pids=()
for i in $(seq 1 $cpu_stress_cores); do
    (
        while [ "$(date +%s)" -lt "$end_time" ]; do
            : # busy-loop
        done
    ) &
    cpu_pids+=($!)
done

########################
# Memory Stress
# Run in its own subshell so CPU saturation doesn't starve it
########################
echo "Starting memory stress..."
(
    while [ "$(date +%s)" -lt "$end_time" ]; do
        dd if=/dev/zero of=/dev/shm/stress.$RANDOM bs=200M count=1 2>/dev/null
        sleep 0.2
    done
) &
mem_pid=$!

########################
# Wait until end time
########################
echo "Stress test running... waiting until end time."
while [ "$(date +%s)" -lt "$end_time" ]; do
    sleep 10
done

echo "Time reached. Stopping stress processes..."

########################
# CPU Cleanup
########################
for pid in "${cpu_pids[@]}"; do
    kill "$pid" 2>/dev/null
    wait "$pid" 2>/dev/null
done

########################
# Memory Cleanup
########################
kill "$mem_pid" 2>/dev/null
wait "$mem_pid" 2>/dev/null
rm -f /dev/shm/stress.*

echo "Stress test complete. All resources released."
