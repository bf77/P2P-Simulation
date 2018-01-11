# Simulation

### Install

```yum install java-1.8.0-openjdk-devel.x86_64```

### Usage

```MAX_NODE 1000~80000```

The operation has been confirmed.
If it doesn't work, it's okay to increase the cache or distribute time to join.
**More than 80,000 nodes, java.lang.OutOfMemoryError: Java heap space occurs.**



### Version

#### Real Time Mode
The process may be inaccurate.

- 80000 Node (Example settings)

```
DEFAULT_CACHE 3000.0id
CACHE_TLV 2400.0id
BOUND_TIME_JOIN 0~1min
```

#### Non Real Time Mode
It can be handled accurately.

- 80000 Node (Example settings)

```
DEFAULT_CACHE 3000.0id
CACHE_TLV 2400.0id
BOUND_TIME_JOIN 0~15min
```