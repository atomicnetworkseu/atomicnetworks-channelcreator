package eu.atomicnetworks.jts3servermod.channelcreator.managers;

import com.mongodb.ConnectionString;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import eu.atomicnetworks.jts3servermod.channelcreator.ChannelCreator;
import org.bson.Document;

/**
 *
 * @author Kacper Mura
 * 2019 - 2020 Copyright (c) All rights reserved.
 * GitHub: https://github.com/VocalZero
 *
 */
public class MongoManager {

    private final ChannelCreator channelCreator;
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> channels;

    public MongoManager(ChannelCreator channelCreator) {
        this.channelCreator = channelCreator;
        this.client = MongoClients.create(new ConnectionString("mongodb://127.0.0.1"));
        this.database = client.getDatabase("teamspeak");
        this.channels = this.database.getCollection("channels");
    }

    public MongoCollection<Document> getCollection(String name) {
        return this.database.getCollection(name);
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoCollection<Document> getChannels() {
        return channels;
    }

}
