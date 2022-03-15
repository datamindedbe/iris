#!/bin/bash

#
storm nimbus -c topology.worker.max.heap.size.mb=65536 -c worker.heap.memory.mb=65536

sleep 10s

#storm jar storm-tutorial-2.3.0.jar tutorial.NumberTopology

# Wait for any process to exit
wait -n

# Exit with status of process that exited first
exit $?