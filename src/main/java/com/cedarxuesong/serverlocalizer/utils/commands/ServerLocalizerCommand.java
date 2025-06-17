package com.cedarxuesong.serverlocalizer.utils.commands;

import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.Arrays;

/**
 * 主命令类，协调所有子命令的处理
 */
public class ServerLocalizerCommand extends CommandBase {
    private static final String TAG = "ServerLocalizerCommand";
    
    // Flag to signal GUI opening
    public static boolean shouldOpenConfigGui = false;

    // 子命令处理器
    private final SubCommandHandler translateHandler;
    
    public ServerLocalizerCommand() {
        // 初始化子命令处理器
        translateHandler = new TranslateCommandHandler();
    }
    
    @Override
    public String getCommandName() {
        return "serverlocalizer";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/serverlocalizer <translate|config|apiconfig> [args...]";
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            showUsage(sender);
            return;
        }
        
        String subCommand = args[0];
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        switch (subCommand) {
            case "translate":
                translateHandler.handleCommand(sender, subArgs);
                break;
            case "config":
                openConfigGui();
                break;
            default:
                showUsage(sender);
                break;
        }
    }
    
    private void openConfigGui() {
        mylog.log(TAG, "接收到打开GUI的指令，设置标志位...");
        shouldOpenConfigGui = true;
    }
    
    private void showUsage(ICommandSender sender) {
        IChatComponent usageMessage = new ChatComponentText(
                "§e=== ServerLocalizer 命令帮助 ===\n" +
                "§b/serverlocalizer translate <messageId> §f- 翻译聊天消息\n" +
                "§b/serverlocalizer config §f- 打开配置界面\n" +
                "§7输入子命令获取更多帮助"
        );
        sender.addChatMessage(usageMessage);
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0; // 所有玩家都可以使用此命令
    }
    
    /**
     * 子命令处理器接口
     */
    public interface SubCommandHandler {
        void handleCommand(ICommandSender sender, String[] args);
    }
    
    /**
     * 翻译命令处理器
     */
    private static class TranslateCommandHandler implements SubCommandHandler {
        private final ChatTranslateCommand command = new ChatTranslateCommand();
        
        @Override
        public void handleCommand(ICommandSender sender, String[] args) {
            command.processSubCommand(sender, args);
        }
    }
} 