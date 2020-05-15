<img src="https://avatars1.githubusercontent.com/u/16785313?s=96&v=4" alt="Heroslender" title="Heroslender" align="right" height="96" width="96"/>

# HeroSpawners

[![GitHub stars](https://img.shields.io/github/stars/heroslender/HeroSpawners.svg)](https://github.com/heroslender/HeroSpawners/stargazers)
[![bStats Servers](https://img.shields.io/bstats/servers/2088.svg?color=1bcc1b)](https://bstats.org/plugin/bukkit/HeroSpawners)
[![GitHub All Releases](https://img.shields.io/github/downloads/heroslender/HeroSpawners/total.svg?logoColor=fff)](https://github.com/heroslender/HeroSpawners/releases/latest)
[![GitHub issues](https://img.shields.io/github/issues-raw/heroslender/HeroSpawners.svg?label=issues)](https://github.com/heroslender/HeroSpawners/issues)
[![GitHub last commit](https://img.shields.io/github/last-commit/heroslender/HeroSpawners.svg)](https://github.com/heroslender/HeroSpawners/commit)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/218d46cbc31345f2ac94e204641e91ff)](https://app.codacy.com/app/heroslender/HeroSpawners?utm_source=github.com&utm_medium=referral&utm_content=heroslender/HeroSpawners&utm_campaign=Badge_Grade_Dashboard)
[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badges/)

O HeroSpawners permite que os spawners do seu servidor agrupem, reduzindo assim o lag no cliente e no servidor.

![Preview](https://github.com/heroslender/HeroSpawners/raw/develop/assets/preview_hologram.gif)
![Preview](https://github.com/heroslender/HeroSpawners/raw/develop/assets/preview.gif)

## Comandos

- `/herospawners reload` - Comando para recarregar a configuração do plugin.
- `/spawners <player> [entity] [stack size] [multiplier]` - Comando para pegar spawners(Utilizavel apenas se tiver o 
sistema de spawners ativo).

## Permissões

- `herospawners.admin` - Permissão para usar o comando de recarregar a configuração.

## Configuração

```yaml
MySql:
  # Usar MySql? Se sim alterar para true
  usar: false
  host: localhost
  port: 3306
  database: herospawners
  user: root
  pass: ''
juntar:
  # Raio para procurar spawners para agrupar ao colocar
  raio: 5
  # Limite de spawners por stack, 0 = infinito
  maximo: 0
# Configuração do sistema de spawners
spawner:
  # Ativar o sistema de spawners interno?
  enable: true
   # Dropar xp quando quebra spawner
  dropXP: false
  # Configuração do item dos spawners. Placeholders disponiveis:
  #  > %tipo% -> Nome do mob
  #  > %quantidade% -> Quantidade de spawners no stack
  # PS: O placeholder da quantidade é obrigatório colocar, seja no nome ou seja na lore!
  ItemStack:
    # Nome do item
    name: '&7Gerador de Monstros'
    # Lore do item
    lore:
    - '&eTipo: &7%tipo%'
    - '&eQuantidade: &7%quantidade%asds'
  # Sistema de silktouch - Quebrar spawners requer silktouch
  SilkTouch:
    # Ativar o sistema?
    enable: true
    # Nivel minimo de silktouch, se usar SilkTouch II por exemplo, só trocar para 2
    minLevel: 1
    # O que acontece quando quebra spawner sem ter o SilkTouch requirido?
    # Se colocar em true, quebra o spawner normal, mas não dropa o item
    # Se colocar em false, não quebra spawner nem dropa o item
    detroySpawnerWithouSilktouch: true
holograma:
  # Distância a que o holograma do spawner fica visivel
  distancia: 5
  # Texto do holograma
  # Placeholders disponiveis
  #  > %quantidade% -> Quantidade de spawners no stack
  #  > %tipo% -> Nome do mob que spawna
  #  > %dono% -> Dono do spawner/quem o colocou
  #  > %skull% -> Cabeça do mob, tem que ser uma linha dedicada
  texto:
    - '&7%quantidade%x &e%tipo%'
    - '&eDono: &7%dono%'
    - '%skull%'
# Configuração de cada mob
mobs:
  CREEPER:
    # Nome a aparecer no holograma do spawner
    name: Creeper
    # Nome da skin da cabeça a mostrar junto com o holograma
    head: MHF_Creeper
  ZOMBIE:
    name: Zombie
    head: MHF_Zombie
```

## API
Hook to HeroSpawners using it's API

### Events
- SpawnerSpawnStackEvent - Called when a stacked spawner spawns an entity stack.
```Java
@EventHandler
private void onSpawnStack(SpawnerSpawnStackEvent e) {
    // You have access to the spawner object containing,
    // for example, the spawner owner.
    ISpawner spawner = e.getSpawner();
    Bukkit.broadcastMessage(spawner.getOwner() + "'s spawner spawned " + e.getStackSize() + "x of" + spawner.getEntityProperties().getDisplayName());

    yourPlugin.createStack(e.getEntityType(), e.getStackSize());
    // Don't forget to cancell the spawn event if you created the stack :)
    e.setCancelled(true);
}
```