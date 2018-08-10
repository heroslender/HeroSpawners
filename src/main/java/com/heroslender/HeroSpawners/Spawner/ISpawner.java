package com.heroslender.HeroSpawners.Spawner;

public interface ISpawner {

    /**
     * Atualizar o holograma ou criar denovo
     * caso tenha desaparecido
     */
    void reloadHologram();

    /**
     * Definir a quanntidade de spawners stackados
     * O Holograma é atualizado automaticamente
     * @param quatidade Quantidade de spawners stackados
     */
    void setQuatidade(int quatidade);

    /**
     * Pegar a quantidade de spawners stackados
     * @return Numero de spawners stackados
     */
    int getQuatidade();

    /**
     * Apagar o spawner da Base de Dados
     * e remover o holograma
     */
    void destroy();
}
