package eu.atomicnetworks.jts3servermod.channelcreator;

import com.google.gson.Gson;
import de.stefan1200.jts3servermod.interfaces.HandleBotEvents;
import de.stefan1200.jts3servermod.interfaces.HandleTS3Events;
import de.stefan1200.jts3servermod.interfaces.JTS3ServerMod_Interface;
import de.stefan1200.jts3serverquery.JTS3ServerQuery;
import de.stefan1200.jts3serverquery.TS3ServerQueryException;
import eu.atomicnetworks.jts3servermod.channelcreator.managers.ChannelManager;
import eu.atomicnetworks.jts3servermod.channelcreator.managers.MongoManager;
import eu.atomicnetworks.jts3servermod.channelcreator.objects.Channel;
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
    private MongoManager mongoManager;
    private ChannelManager channelManager;
    private Gson gson;
    
    private final int channel_creator = 84;
    private final int channel_order = 209;
    private final int channel_role = 9;
    
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
        this.mongoManager = new MongoManager(this);
        this.channelManager = new ChannelManager(this);
        this.gson = new Gson();
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
                HashMap<String, String> clientInfo = this.modClass.getClientListEntry(Integer.valueOf(eventInfo.get("clid")));
                if(this.getChannelManager().getChannel(this.queryLib.decodeTS3String(clientInfo.get("client_unique_identifier"))).getChannelId() != 0) {
                    Channel channel = this.getChannelManager().getChannel(this.queryLib.decodeTS3String(clientInfo.get("client_unique_identifier")));
                    HashMap<String, String> channelReponse = this.queryLib.doCommand(MessageFormat.format("channelinfo cid={0}", channel.getChannelId()));
                    if(channelReponse.get("msg").equalsIgnoreCase("invalid channelID")) {
                        try {
                            this.queryLib.sendTextMessage(Integer.valueOf(eventInfo.get("clid")), JTS3ServerQuery.TEXTMESSAGE_TARGET_CLIENT, "[B]ChannelCreator[/B] » Your channel has been deleted because you haven't used it for a long time, we will create a new one...");
                        } catch (TS3ServerQueryException ex) {
                            Logger.getLogger(ChannelCreator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        this.createChannel(eventInfo, clientInfo);
                        return;
                    }
                    try {
                        this.queryLib.moveClient(Integer.valueOf(eventInfo.get("clid")), channel.getChannelId(), "");
                    } catch (TS3ServerQueryException ex) {
                        Logger.getLogger(ChannelCreator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return;
                }
                this.createChannel(eventInfo, clientInfo);
            }
        }
    }
    
    private void createChannel(HashMap<String, String> eventInfo, HashMap<String, String> clientInfo) {
        HashMap<String, String> channelReponse = this.queryLib.doCommand(MessageFormat.format("channelcreate channel_name={0} channel_topic={1} channel_flag_permanent=1 channel_order={2}", this.queryLib.encodeTS3String(this.queryLib.decodeTS3String(clientInfo.get("client_nickname")) + "'s Channel"), this.queryLib.encodeTS3String("🐹  Created by " + this.queryLib.decodeTS3String(clientInfo.get("client_nickname") + " • " + this.queryLib.decodeTS3String(clientInfo.get("client_unique_identifier")))), this.channel_order));
        HashMap<String, String> channelInfo = getTS3Reponse(channelReponse.get("response").split(" "));
        try {
            this.queryLib.moveClient(Integer.valueOf(eventInfo.get("clid")), Integer.valueOf(channelInfo.get("cid")), "");
            this.queryLib.doCommand(MessageFormat.format("setclientchannelgroup cgid={0} cid={1} cldbid={2}", this.channel_role, Integer.valueOf(channelInfo.get("cid")), Integer.valueOf(clientInfo.get("client_database_id"))));
            this.queryLib.sendTextMessage(Integer.valueOf(eventInfo.get("clid")), JTS3ServerQuery.TEXTMESSAGE_TARGET_CLIENT, "[B]ChannelCreator[/B] » You've been moved to your channel.");
            this.getChannelManager().getChannelCache().invalidate(this.queryLib.decodeTS3String(clientInfo.get("client_unique_identifier")));
            this.getChannelManager().createChannel(this.queryLib.decodeTS3String(clientInfo.get("client_unique_identifier")), Integer.valueOf(channelInfo.get("cid")), (Channel t) -> {
                Channel channel = this.getChannelManager().getChannel(this.queryLib.decodeTS3String(clientInfo.get("client_unique_identifier")));
            });
        } catch (TS3ServerQueryException ex) {
            Logger.getLogger(ChannelCreator.class.getName()).log(Level.SEVERE, null, ex);
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

    public MongoManager getMongoManager() {
        return mongoManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public Gson getGson() {
        return gson;
    }
    
}
