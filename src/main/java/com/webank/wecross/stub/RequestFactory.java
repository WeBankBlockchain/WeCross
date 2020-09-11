public class RequestFactory {
    public static Request requestBuilder(int type, String content) {
        return requestBuilder(type, content.getBytes(StandardCharsets.UTF_8));
    }

    public static Request requestBuilder(int type, byte[] content) {
        Request request = new Request();
        request.setType(type);
        request.setData(content);
        return request;
    }
}
