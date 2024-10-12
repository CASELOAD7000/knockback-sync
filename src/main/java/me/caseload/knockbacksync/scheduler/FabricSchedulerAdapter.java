//package me.caseload.knockbacksync.scheduler;
//
//import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
//import net.minecraft.server.MinecraftServer;
//
//public class FabricSchedulerAdapter implements SchedulerAdapter {
//    private final Object plugin; // You might need to change this to whatever represents your mod in Fabric
//    private final MinecraftServer server;
//
//    public FabricSchedulerAdapter(Object plugin, MinecraftServer server) {
//        this.plugin = plugin;
//        this.server = server;
//    }
//
//    @Override
//    public AbstractTaskHandle runTask(Runnable task) {
//        return new AbstractTaskHandle(ServerTickEvents.END_SERVER_TICK.register(server -> task.run()));
//    }
//
//    @Override
//    public AbstractTaskHandle runTaskAsynchronously(Runnable task) {
//        Thread thread = new Thread(task);
//        thread.start();
//        return new AbstractTaskHandle(() -> thread.interrupt());
//    }
//
//    @Override
//    public AbstractTaskHandle runTaskLater(Runnable task, long delayTicks) {
//        long targetTick = server.getTicks() + delayTicks;
//        ServerTickEvents.EndTick event = server -> {
//            if (server.getTicks() >= targetTick) {
//                task.run();
//                ServerTickEvents.END_SERVER_TICK.unregister(this);
//            }
//        };
//        ServerTickEvents.END_SERVER_TICK.register(event);
//        return new AbstractTaskHandle(() -> ServerTickEvents.END_SERVER_TICK.unregister(event));
//    }
//
//    @Override
//    public AbstractTaskHandle runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
//        long nextRunTick = server.getTicks() + delayTicks;
//        ServerTickEvents.EndTick event = server -> {
//            if (server.getTicks() >= nextRunTick) {
//                task.run();
//                nextRunTick = server.getTicks() + periodTicks;
//            }
//        };
//        ServerTickEvents.END_SERVER_TICK.register(event);
//        return new AbstractTaskHandle(() -> ServerTickEvents.END_SERVER_TICK.unregister(event));
//    }
//
//    @Override
//    public AbstractTaskHandle runTaskLaterAsynchronously(Runnable task, long delay) {
//        Thread thread = new Thread(() -> {
//            try {
//                Thread.sleep(delay * 50); // Convert ticks to milliseconds
//                task.run();
//            } catch (InterruptedException e) {
//                // Handle interruption
//            }
//        });
//        thread.start();
//        return new AbstractTaskHandle(() -> thread.interrupt());
//    }
//
//    @Override
//    public AbstractTaskHandle runTaskTimerAsynchronously(Runnable task, long delay, long period) {
//        Thread thread = new Thread(() -> {
//            try {
//                Thread.sleep(delay * 50); // Convert ticks to milliseconds
//                while (!Thread.currentThread().isInterrupted()) {
//                    task.run();
//                    Thread.sleep(period * 50); // Convert ticks to milliseconds
//                }
//            } catch (InterruptedException e) {
//                // Handle interruption
//            }
//        });
//        thread.start();
//        return new AbstractTaskHandle(() -> thread.interrupt());
//    }
//}