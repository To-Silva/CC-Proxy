
## CC ##

> measuring unit (CPU)
> segundo caso   (RAM)
> terceiro caso  (requests a processar)

_??/04_

> monitorUDP terá de "pingar" o servidor com
> a info de CPU/RAM/requests a cada x segundos

> se o servidor não recebeu informação passado
> x segundos, o servidor envia um N-ACK a cada
> x segundos, e invalida a entrada prévia com
> informação anterior do monitor (até receber novo update)

> se passado (3*x) segundos o servidor não
> tiver recebido nenhuma informação, remove-o da tabela

_02/05_

## register info ##

|  tipo     | CPU-Load/Benchmark |  IP | seq.numb |
| :-------: | :----------------: | :-: | :------: |
| registo 0 |      benchmark     |  ip |     0    |
|    1      |      cpu-load      |  ip |  numb++  |
|  not 1    |        null        | N/A |   N/A    |
