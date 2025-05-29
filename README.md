Read about Maelstrom [here](https://github.com/jepsen-io/maelstrom).

# Requirements

Implement a broadcast system that gossips messages between all nodes in the cluster

- [x] Handle the "broadcast" workload.

```shell
maelstrom test -w broadcast --bin run.sh --node-count 1 --time-limit 20 --rate 10
```

- [x] Propagate broadcasted messages to the other nodes in the cluster.

```shell
maelstrom  test -w broadcast --bin run.sh --node-count 5 --time-limit 20 --rate 10
```

- [x] Handle network partitions.

 ```shell
maelstrom test -w broadcast --bin run.sh --node-count 5 --time-limit 20 --rate 10 --nemesis partition
```

- [x] Meet minimum performance targets.
    - [x] :net:server:msgs-per-op < 30
    - [x] :workload:stable-latencies:0.5 < 400
    - [x] :workload:stable-latencies:1 < 600

```shell
maelstrom test -w broadcast --bin run.sh --node-count 25 --time-limit 20 --rate 100 
```

- [x] Meet ideal performance targets.
    - [x] :net:server:msgs-per-op < 20
    - [x] :workload:stable-latencies:0.5 < 1000
    - [x] :workload:stable-latencies:1 < 2000

```shell
maelstrom test -w broadcast --bin run.sh --node-count 25 --time-limit 20 --rate 100 
```
