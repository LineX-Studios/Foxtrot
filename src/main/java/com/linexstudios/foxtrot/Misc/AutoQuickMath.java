package com.linexstudios.foxtrot.Misc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoQuickMath {
    public static final AutoQuickMath instance = new AutoQuickMath();
    
    // GUI Toggles
    public static boolean enabled = true;
    public static int randomMode = 1; // 0 = Normal, 1 = Extra, 2 = Extra+
    public static float baseDelayMs = 1500f; // Slider value (0 to 5000)

    @SubscribeEvent
    public void solveQuickMath(ClientChatReceivedEvent event){
        String rawMessage = event.message.getUnformattedText();
        if(enabled && rawMessage.startsWith("QUICK MATHS! Solve: ") && Minecraft.getMinecraft().currentScreen == null){
            long millisStarted = System.currentTimeMillis();
            try{
                String mathProblem = rawMessage.replace("QUICK MATHS! Solve: ", "");
                new Thread(()->{
                    try {
                        int delay = (int) baseDelayMs;
                        
                        // Apply selected randomization mode
                        if (randomMode == 1) {
                            delay += (int) (Math.random() * 1000);
                        } else if (randomMode == 2) {
                            delay += (int) (Math.random() * 2000);
                        }
                        
                        if (delay > 0) {
                            Thread.sleep(delay); // Natural delay to avoid anti-cheat
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    long millisToSolve = System.currentTimeMillis() - millisStarted;
                    
                    String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "Foxtrot" + EnumChatFormatting.GRAY + "] ";
                    String content = EnumChatFormatting.GREEN + "Solved math problem " + EnumChatFormatting.GRAY + "(" + millisToSolve + "ms)";
                    
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(prefix + content));
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("" + (int) eval(mathProblem.replace("x", "*")));
                }).start();
            }
            catch (Exception ignored){ }
        }
    }

    private static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;
            void nextChar() { ch = (++pos < str.length()) ? str.charAt(pos) : -1; }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); 
                    else if (eat('-')) x -= parseTerm(); 
                    else return x;
                }
            }
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); 
                    else if (eat('/')) x /= parseFactor(); 
                    else return x;
                }
            }
            double parseFactor() {
                if (eat('+')) return parseFactor(); 
                if (eat('-')) return -parseFactor(); 

                double x;
                int startPos = this.pos;
                if (eat('(')) { 
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { 
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                if (eat('^')) x = Math.pow(x, parseFactor()); 
                return x;
            }
        }.parse();
    }
}