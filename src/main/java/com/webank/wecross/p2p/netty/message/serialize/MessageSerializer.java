package com.webank.wecross.p2p.netty.message.serialize;

import com.webank.wecross.p2p.netty.message.proto.Message;
import io.netty.buffer.ByteBuf;
import java.io.UnsupportedEncodingException;

public class MessageSerializer {

    private void readHeader(Message message, ByteBuf in) throws UnsupportedEncodingException {
        Integer length = in.readInt();
        Short type = in.readShort();

        byte[] dst = new byte[32];
        in.readBytes(dst);
        String seq = new String(dst, "utf-8");

        Integer result = in.readInt();

        message.setLength(length);
        message.setType(type);
        message.setSeq(seq);
        message.setResult(result);
    }

    private void readData(Message message, ByteBuf in) {
        byte[] data = new byte[message.getLength() - Message.HEADER_LENGTH];
        in.readBytes(data, 0, message.getLength() - Message.HEADER_LENGTH);
        message.setData(data);
    }

    private void writeHeader(Message message, ByteBuf out) {
        out.writeInt(Message.HEADER_LENGTH + message.getData().length);
        out.writeShort(message.getType());
        out.writeBytes(message.getSeq().getBytes(), 0, 32);
        out.writeInt(message.getResult());
    }

    private void writeData(Message message, ByteBuf out) {
        out.writeBytes(message.getData());
    }

    public Message deserialize(ByteBuf byteBuf) throws UnsupportedEncodingException {
        Message message = new Message();
        readHeader(message, byteBuf);
        readData(message, byteBuf);
        return message;
    }

    public void serialize(Message message, ByteBuf byteBuf) {
        writeHeader(message, byteBuf);
        writeData(message, byteBuf);
    }
}
