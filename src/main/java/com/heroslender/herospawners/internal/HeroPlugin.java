package com.heroslender.herospawners.internal;

import com.heroslender.herospawners.service.Service;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("UnusedReturnValue")
public class HeroPlugin extends JavaPlugin {
    private final List<Service> services;

    public HeroPlugin() {
        this.services = new ArrayList<>();
    }

    @Override
    public final void onEnable() {
        enable();
        enableServices();
    }

    @Override
    public final void onDisable() {
        disableServices();
        disable();

        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
        Bukkit.getServicesManager().unregisterAll(this);
    }

    public void enable() {

    }

    public void disable() {

    }

    public void reload() {
        disableServices();
        reloadConfig();
        enableServices();
    }

    private void enableServices() {
        for (Service service : this.services) {
            service.enable();
        }
    }

    private void disableServices() {
        for (int i = this.services.size() - 1; i >= 0; i--) {
            services.get(i).disable();
        }
    }

    public <T extends Listener> T registerListener(@NotNull T listener) {
        Objects.requireNonNull(listener, "listener");
        getServer().getPluginManager().registerEvents(listener, this);

        return listener;
    }

    @SafeVarargs
    public final <T extends Listener> T[] registerListener(T... listeners) {
        for (T listener : listeners) {
            registerListener(listener);
        }

        return listeners;
    }

    @NotNull
    public <T> T getService(Class<T> service) {
        Objects.requireNonNull(service, "clazz");

        return Optional
            .ofNullable(Bukkit.getServicesManager().getRegistration(service))
            .map(RegisteredServiceProvider::getProvider)
            .orElseThrow(() -> new IllegalStateException("No registration present for service '" + service.getName() + "'"));
    }

    public <T extends Service> T provideService(@NotNull Class<T> clazz, @NotNull T instance, @NotNull ServicePriority priority) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(priority, "priority");

        Bukkit.getServicesManager().register(clazz, instance, this, priority);
        this.services.add(instance);

        return instance;
    }

    public <T extends Service> T provideService(Class<T> clazz, T instance) {
        provideService(clazz, instance, ServicePriority.Normal);

        return instance;
    }
}