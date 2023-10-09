# EIFuzzCND

Este repositório contém o código-fonte e informações sobre o desenvolvimento de um algoritmo projetado para lidar com cenários de detecção de novidades em fluxo de dados. O algoritmo apresentado aqui utiliza uma abordagem offline/online para aprimorar a precisão da detecção de novidades em tempo real.

## Sobre o Algoritmo

O EIFuzzCND é uma evolução do algoritmo EFuzzCND, que pode ser encontrado [neste repositório](https://github.com/andrecristiani/EFuzzCND-Results). O principal objetivo da criação deste algoritmo é aprimorar a capacidade de detecção de novidades em fluxo de dados, abordando dois principais aspectos:

### 1. Abordagem Incremental

Uma das principais melhorias implementadas no EIFuzzCND é a introdução de uma abordagem incremental no Modelo de Classes Conhecidas (MCC). Isso foi feito com o propósito de lidar de forma mais eficaz com desvios de conceito nos dados de entrada. Através dessa abordagem, o algoritmo é capaz de se adaptar continuamente às mudanças nos padrões de dados, garantindo uma detecção de novidades mais precisa.

### 2. Latência Intermediária

Outra importante melhoria no EIFuzzCND é a modificação da forma como o algoritmo se comporta em cenários de latência intermediária. Diferentemente de abordagens tradicionais, este algoritmo não exige o retorno de todos os rótulos verdadeiros ao fluxo de dados em tempo real. Isso proporciona uma maior eficiência no processamento de dados, gerando mais robustez na tarefa de detecção de novidades.

## Fases do Algoritmo

O EIFuzzCND opera em duas fases distintas: Fase Offline e Fase Online. Abaixo, você encontrará uma explicação detalhada de cada uma delas, juntamente com os fluxogramas correspondentes.

### Fase Offline

Na Fase Offline, o algoritmo realiza as seguintes tarefas:

1. Separação do conjunto de treinamento rotulado por classe
2. Treinamento de cada subconjunto utilizando Fuzzy C-Means Clustering
3. Sumarização e criação dos micro-grupos (SPFMiCs) gerando o Modelo de classes conhecidas (MCC)

### Fluxograma em PDF

Você pode acessar a documentação em PDF [aqui](https://github.com/lucas-bruzzone/EIFuzzCND/raw/main/graphics/_Fluxograma%20Fase%20Offline.pdf).


### Fase Online

Na Fase Online, o algoritmo está em execução contínua e realiza a detecção de novidades em tempo real. As etapas incluem:

1. Tentativa de classificação pelo MCC e Modelo de classes desconhecidas (MCD) 
2. Abordagem incremental para atualização do MCC
3. Detecção de novidadedes e manutenção do MCD
4. Atualização da matriz de confusão incremental (MCI)

### Fluxograma em PDF

Você pode acessar a documentação em PDF [aqui](https://github.com/lucas-bruzzone/EIFuzzCND/raw/main/graphics/_Fluxograma%20Fase%20Online.pdf).

### Dissertação ( Ainda sem correções finais ) 

A dissertação completa pode ser encontrada [aqui](https://github.com/lucas-bruzzone/EIFuzzCND/blob/main/Dissertação_Mestrado.pdf).


## Autores

- [Lucas Ricardo Duarte Bruzzone](https://github.com/lucas-bruzzone) - Colaborador Principal
