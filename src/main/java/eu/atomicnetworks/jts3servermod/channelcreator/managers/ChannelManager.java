package eu.atomicnetworks.jts3servermod.channelcreator.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import eu.atomicnetworks.jts3servermod.channelcreator.ChannelCreator;
import eu.atomicnetworks.jts3servermod.channelcreator.objects.Channel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bson.Document;

/**
 *
 * @author Kacper Mura
 * 2019 - 2020 Copyright (c) All rights reserved.
 * GitHub: https://github.com/VocalZero
 *
 */
public class ChannelManager {
    
    private final ChannelCreator channelCreator;
    private LoadingCache<String, Channel> channelCache;

    public ChannelManager(ChannelCreator channelCreator) {
        this.channelCreator = channelCreator;
        this.initCache();
    }
    
    private void initCache() {
        this.channelCache = CacheBuilder.newBuilder().maximumSize(100L).expireAfterWrite(30L, TimeUnit.MINUTES).build(new CacheLoader<String, Channel>() {
            @Override
            public Channel load(String clientUid) throws Exception {
                CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
                getChannel(clientUid, result -> {
                    completableFuture.complete(result);
                });
                return completableFuture.get();
            }
        });
    }
    
    public void createChannel(String clientUid, int channelId, Consumer<Channel> consumer) {
        this.channelCreator.getMongoManager().getChannels().find(Filters.eq("clientUid", clientUid)).first((Document t, Throwable thrwbl) -> {
            if(t == null) {
                Channel channel = new Channel();
                channel.setClientUid(clientUid);
                channel.setChannelId(channelId);
                
                t = this.channelCreator.getGson().fromJson(this.channelCreator.getGson().toJson(channel), Document.class);
                this.channelCreator.getMongoManager().getChannels().insertOne(t, (Void t1, Throwable thrwbl1) -> {
                    consumer.accept(channel);
                });
            } else {
                Channel channel = new Channel();
                channel.setClientUid(clientUid);
                channel.setChannelId(channelId);
                Document document = this.channelCreator.getGson().fromJson(this.channelCreator.getGson().toJson(channel), Document.class);
                this.channelCreator.getMongoManager().getChannels().replaceOne(Filters.eq("clientUid", clientUid), document, (UpdateResult t1, Throwable thrwbl1) -> {
                });
            }
        });
    }
    
    private void getChannel(String clientUid, Consumer<Channel> consumer) {
        this.channelCreator.getMongoManager().getChannels().find(Filters.eq("clientUid", clientUid)).first((Document t, Throwable thrwbl) -> {
            if(t != null) {
                Channel channel = this.channelCreator.getGson().fromJson(t.toJson(), Channel.class);
                consumer.accept(channel);
            } else {
                Channel channel = new Channel();
                channel.setClientUid(clientUid);
                channel.setChannelId(0);
                consumer.accept(channel);
            }
        });
    }

    public LoadingCache<String, Channel> getChannelCache() {
        return channelCache;
    }
    
    public Channel getChannel(String clientUid) {
        try {
            return this.channelCache.get(clientUid);
        } catch (ExecutionException ex) {
            return null;
        }
    }
    
}
