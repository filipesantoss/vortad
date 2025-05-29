Read about Maelstrom [here](https://github.com/jepsen-io/maelstrom).

# Requirements

Implement a broadcast system that gossips messages between all nodes in the cluster

- [x] Handle the "broadcast" workload.

```shell
maelstrom test -w broadcast --bin run.sh --node-count 1 --time-limit 20 --rate 10
```

- [x] Propagate values broadcasted messages to the other nodes in the cluster.

```shell
maelstrom  test -w broadcast --bin run.sh --node-count 5 --time-limit 20 --rate 10
```

- [x] Handle network partitions.

 ```shell
maelstrom test -w broadcast --bin run.sh --node-count 5 --time-limit 20 --rate 10 --nemesis partition
```