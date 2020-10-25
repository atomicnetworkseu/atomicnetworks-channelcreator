package eu.atomicnetworks.jts3servermod.channelcreator;

import de.stefan1200.jts3servermod.interfaces.HandleBotEvents;
import de.stefan1200.jts3servermod.interfaces.HandleTS3Events;
import de.stefan1200.jts3servermod.interfaces.JTS3ServerMod_Interface;
import de.stefan1200.jts3serverquery.JTS3ServerQuery;
import de.stefan1200.jts3serverquery.TS3ServerQueryException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kacper Mura
 * 2019 - 2020 Copyright (c) All rights reserved.
 * GitHub: https://github.com/VocalZero
 *
 */
public class ChannelCreator implements HandleBotEvents, HandleTS3Events {
    
    private JTS3ServerMod_Interface modClass = null;
    private JTS3ServerQuery queryLib = null;
    
    private int channel_creator = 20;
    private int channel_order = 20;
    private int channel_role = 5;
    
    public static void main(String[] args) {
    }

    @Override
    public void initClass(JTS3ServerMod_Interface modClass, JTS3ServerQuery queryLib, String prefix) {
        this.modClass = modClass;
        this.queryLib = queryLib;
    }

    @Override
    public void handleOnBotConnect() {
    }

    @Override
    public void handleAfterCacheUpdate() {
    }

    @Override
    public void activate() {
        System.out.println("ChannelCreator Plugin v1.0 created by Kacper Mura (VocalZero) https://github.com/VocalZero.");
    }

    @Override
    public void disable() {
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean multipleInstances() {
        return false;
    }

    @Override
    public int getAPIBuild() {
        return 4;
    }

    @Override
    public String getCopyright() {
        return "ChannelCreator Plugin v1.0 created by Kacper Mura (VocalZero) [url]https://github.com/VocalZero[/url].";
    }

    @Override
    public String[] botChatCommandList(HashMap<String, String> eventInfo, boolean isFullAdmin, boolean isAdmin) {
        return null;
    }

    @Override
    public String botChatCommandHelp(String command) {
        return "";
    }

    @Override
    public boolean handleChatCommands(String msg, HashMap<String, String> eventInfo, boolean isFullAdmin, boolean isAdmin) {
        return false;
    }

    @Override
    public void handleTS3Events(String eventType, HashMap<String, String> eventInfo) {
        if(eventType.equalsIgnoreCase("notifyclientmoved")) {
            if(Integer.valueOf(eventInfo.get("ctid")) == this.channel_creator) {
                HashMap<String, String> commandResponse = this.queryLib.doCommand(MessageFormat.format("clientinfo clid={0}", Integer.valueOf(eventInfo.get("clid"))));
                HashMap<String, String> clientInfo = getTS3Reponse(commandResponse.get("response").split(" "));
                HashMap<String, String> channelReponse = this.queryLib.doCommand(MessageFormat.format("channelcreate channel_name={0} channel_topic={1} channel_flag_permanent=1 channel_order={2}", this.queryLib.encodeTS3String(this.queryLib.decodeTS3String(clientInfo.get("client_nickname")) + "'s Channel"), this.queryLib.encodeTS3String("🐹  Created by " + this.queryLib.decodeTS3String(clientInfo.get("client_nickname") + " • " + this.queryLib.decodeTS3String(clientInfo.get("client_unique_identifier")))), this.channel_order));
                HashMap<String, String> channelInfo = getTS3Reponse(channelReponse.get("response").split(" "));
                try {
                    this.queryLib.moveClient(Integer.valueOf(eventInfo.get("clid")), Integer.valueOf(channelInfo.get("cid")), "");
                    this.queryLib.doCommand(MessageFormat.format("setclientchannelgroup cgid={0} cid={1} cldbid={2}", this.channel_role, Integer.valueOf(channelInfo.get("cid")), Integer.valueOf(clientInfo.get("client_database_id"))));
                } catch (TS3ServerQueryException ex) {
                    Logger.getLogger(ChannelCreator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private HashMap<String, String> getTS3Reponse(String[] response) {
        HashMap<String, String> responseMap = new HashMap<>();
        for (int i = 0; i < response.length; i++) {
            String responseValue = response[i];
            if (responseValue.contains("=")) {
                String key = responseValue.split("=")[0];
                String value = responseValue.split("=")[1];
                responseMap.put(key, value);
            }
        }
        return responseMap;
    }
    
}
