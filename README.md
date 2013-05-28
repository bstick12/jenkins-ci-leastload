jenkins-ci-leastload
====================

By default Jenkins tries to allocate a jobs to the last node is was executed on. This can result in nodes 
being left idle while other nodes are overloaded. This plugin overrides the default behaviour and assigns jobs
to nodes with the least load. The least load is defined as a node that is idle or the one with the most available 
executors.


