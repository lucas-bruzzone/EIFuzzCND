# EIFuzzCND

Desenvolvimento de um algoritmo para lidar com cenários de detecção de novidades em fluxo de dados.
Este algoritmo utiliza a abordagem offline/online.


Este algoritmo é uma melhoria do algoritmo EFuzzCND -- https://github.com/andrecristiani/EFuzzCND-Results


As principais melhorias podem ser divididas em dois tópicos. 
  -> Implementar uma abordagem incremental no algoritmo supervisionado com o objetivo de lidar melhor com desvios de conceito
  -> Alterar a forma em que o algoritmo se comporta em um cenário de latência intermediária para que não seja necessário o retorno de todos os rótulo verdadeiros
  ao fluxo de dados.
