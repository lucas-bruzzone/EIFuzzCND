# EIFuzzCND

Este repositório contém o código-fonte e informações sobre o desenvolvimento de um algoritmo projetado para lidar com cenários de detecção de novidades em fluxo de dados. O algoritmo apresentado aqui utiliza uma abordagem offline/online para aprimorar a precisão da detecção de novidades em tempo real.

## Sobre o Algoritmo

O EIFuzzCND é uma evolução do algoritmo EFuzzCND, que pode ser encontrado [neste repositório](https://github.com/andrecristiani/EFuzzCND-Results). O principal objetivo da criação deste algoritmo é aprimorar a capacidade de detecção de novidades em fluxo de dados, abordando dois principais aspectos:

### 1. Abordagem Incremental

Uma das principais melhorias implementadas no EIFuzzCND é a introdução de uma abordagem incremental no algoritmo supervisionado. Isso foi feito com o propósito de lidar de forma mais eficaz com desvios de conceito nos dados de entrada. Através dessa abordagem, o algoritmo é capaz de se adaptar continuamente às mudanças nos padrões de dados, garantindo uma detecção de novidades mais precisa.

### 2. Latência Intermediária

Outra importante melhoria no EIFuzzCND é a modificação da forma como o algoritmo se comporta em cenários de latência intermediária. Diferentemente de abordagens tradicionais, este algoritmo não exige o retorno de todos os rótulos verdadeiros ao fluxo de dados em tempo real. Isso proporciona uma maior eficiência no processamento de dados, reduzindo a sobrecarga de comunicação e melhorando a velocidade de detecção de novidades.

## Fases do Algoritmo

O EIFuzzCND opera em duas fases distintas: Fase Offline e Fase Online. Abaixo, você encontrará uma explicação detalhada de cada uma delas, juntamente com os fluxogramas correspondentes.

### Fase Offline

Na Fase Offline, o algoritmo realiza as seguintes tarefas:

1. Preprocessamento dos dados de treinamento.
2. Treinamento do modelo inicial.
3. Adaptação contínua do modelo a novos padrões de dados.

A imagem a seguir ilustra o fluxograma da Fase Offline:

![Fluxograma Fase Offline](https://github.com/lucas-bruzzone/EIFuzzCND/raw/main/graphics/_Fluxograma%20Fase%20Offline.pdf)

### Fase Online

Na Fase Online, o algoritmo está em execução contínua e realiza a detecção de novidades em tempo real. As etapas incluem:

1. Captura de dados em fluxo.
2. Detecção de novidades com base no modelo adaptado.
3. Tomada de decisões em tempo real.

A imagem a seguir ilustra o fluxograma da Fase Online:

![Fluxograma Fase Online](https://github.com/lucas-bruzzone/EIFuzzCND/raw/main/graphics/_Fluxograma%20Fase%20Online.pdf)



## Autores

- [Lucas Ricardo Duarte Bruzzone](https://github.com/lucas-bruzzone) - Colaborador Principal
