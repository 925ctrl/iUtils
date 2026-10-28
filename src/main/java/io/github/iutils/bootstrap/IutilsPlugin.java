package io.github.iutils.bootstrap;

import io.github.iutils.blockrefill.application.RefillPlacedBlockUseCase;
import io.github.iutils.blockrefill.infrastructure.paper.BlockPlaceRefillListener;
import io.github.iutils.common.config.ConfigManager;
import io.github.iutils.common.messaging.MessageFormatter;
import io.github.iutils.toolswap.application.SwapBreakingToolUseCase;
import io.github.iutils.toolswap.infrastructure.paper.ToolBreakSwapListener;
import io.github.iutils.welcome.application.SendWelcomeMessageUseCase;
import io.github.iutils.welcome.infrastructure.paper.PlayerJoinWelcomeListener;
import io.github.iutils.welcome.presentation.AdventureWelcomePresenter;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * Bootstrap entry point for the Iutils Paper plugin.
 * Responsible exclusively for plugin lifecycle, manual dependency composition,
 * event listener registration, command registration, and resource cleanup.
 */
public final class IutilsPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MessageFormatter messageFormatter;
    private AdventureWelcomePresenter welcomePresenter;
    private SendWelcomeMessageUseCase sendWelcomeMessageUseCase;
    private RefillPlacedBlockUseCase refillPlacedBlockUseCase;
    private SwapBreakingToolUseCase swapBreakingToolUseCase;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.loadInitial();

        this.messageFormatter = new MessageFormatter();
        this.welcomePresenter = new AdventureWelcomePresenter(
                this.configManager::getConfig,
                this.messageFormatter,
                uuid -> Optional.ofNullable(getServer().getPlayer(uuid))
        );
        this.sendWelcomeMessageUseCase = new SendWelcomeMessageUseCase(
                this.configManager::getWelcomeSettings,
                this.welcomePresenter
        );
        this.refillPlacedBlockUseCase = new RefillPlacedBlockUseCase(
                this.configManager::getBlockRefillSettings,
                action -> { }
        );
        this.swapBreakingToolUseCase = new SwapBreakingToolUseCase(
                this.configManager::getToolSwapSettings
        );

        registerListeners();
        registerCommands();

        PluginMeta pluginMeta = getPluginMeta();
        StartupSummaryLogger startupSummaryLogger = new StartupSummaryLogger(getLogger());
        startupSummaryLogger.logStartupSummary(
                pluginMeta.getName(),
                pluginMeta.getVersion(),
                pluginMeta.getAuthors(),
                this.configManager.getConfig()
        );
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        this.swapBreakingToolUseCase = null;
        this.refillPlacedBlockUseCase = null;
        this.sendWelcomeMessageUseCase = null;
        this.welcomePresenter = null;
        this.messageFormatter = null;
        this.configManager = null;

        getLogger().info("Iutils se ha desactivado correctamente.");
    }

    private void registerListeners() {
        PlayerJoinWelcomeListener joinListener = new PlayerJoinWelcomeListener(
                this.sendWelcomeMessageUseCase,
                this.welcomePresenter
        );
        BlockPlaceRefillListener blockPlaceRefillListener = new BlockPlaceRefillListener(
                this.refillPlacedBlockUseCase,
                task -> getServer().getScheduler().runTask(this, task)
        );
        getServer().getPluginManager().registerEvents(joinListener, this);
        getServer().getPluginManager().registerEvents(blockPlaceRefillListener, this);
        getServer().getPluginManager().registerEvents(new ToolBreakSwapListener(this.swapBreakingToolUseCase), this);
    }

    private void registerCommands() {
        PluginCommand command = getCommand("iutils");
        if (command == null) {
            getLogger().severe("No se encontró la definición del comando 'iutils' en plugin.yml.");
            return;
        }
        IutilsCommand iutilsCommand = new IutilsCommand(this.configManager, this.messageFormatter);
        command.setExecutor(iutilsCommand);
        command.setTabCompleter(iutilsCommand);
    }
}
