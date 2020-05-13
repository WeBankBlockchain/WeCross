package com.webank.wecross.network.p2p.netty.message.proto;

import com.webank.wecross.network.p2p.netty.common.Utils;
import java.io.Serializable;

/**
 * message proto =>
 * +----------------+---------------+--------------+-----------------+--------------------------------------------------+
 * | | | | | | | | length(4 Byte) | type(2 Byte) | seq(32 Byte)| result(4 Byte) | extend
 * fields(default None) | data( >= 0 Byte ) | | | | | | | |
 * +----------------+---------------+--------------+-----------------+--------------------------------------------------+
 *
 * <p>length: total length of this packet, including length field, so the value of length is: 4 + 2
 * + 32 + 4 + length(data) type: type of this packet, specific value reference P2PMessageType seq:
 * unique value that marks a request result: the status of the p2p message itself extend fields:
 * expand other fields as needed, default None, user can extend the protocol field. data: load data
 */

/** Structure of P2P message */
public class Message implements Serializable {

    private static final long serialVersionUID = -7276897518418560354L;

    // length of fields
    public static final int LENGTH_FIELD_LENGTH = 4;
    public static final int TYPE_FIELD_LENGTH = 2;
    public static final int SEQ_FIELD_LENGTH = 32;
    public static final int RESULT_FIELD_LENGTH = 4;
    public static final int HEADER_LENGTH =
            LENGTH_FIELD_LENGTH + TYPE_FIELD_LENGTH + SEQ_FIELD_LENGTH + RESULT_FIELD_LENGTH;

    protected Integer length = 0;
    protected Short type = 0;
    protected String seq = Utils.newUUID();
    protected Integer result = 0;
    protected byte[] data = new byte[0];

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        setLength(data.length + HEADER_LENGTH);
    }

    @Override
    public String toString() {
        return "Message{"
                + "length="
                + length
                + ", type="
                + type
                + ", seq='"
                + seq
                + '\''
                + ", result="
                + result
                + ", data.length="
                + data.length
                + '}';
    }

    public static Message builder(Short type) {
        Message message = new Message();
        message.setType(type);
        return message;
    }

    public static Message builder(Short type, String content) {
        Message message = new Message();
        message.setType(type);
        message.setData(content.getBytes());
        return message;
    }
}
