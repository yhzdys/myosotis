package com.yhzdys.myosotis.web;

import com.yhzdys.myosotis.compress.Lz4;
import com.yhzdys.myosotis.constant.NetConst;
import com.yhzdys.myosotis.entity.PollingData;
import com.yhzdys.myosotis.enums.SerializeType;
import com.yhzdys.myosotis.exception.MyosotisException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class RequestDeserializer {

    public static List<PollingData> deserializePollingData(byte[] data) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new MyosotisException("Servlet request attributes is null");
        }
        HttpServletRequest request = attributes.getRequest();
        int length = request.getIntHeader(NetConst.origin_data_length);
        if (length > 0) {
            data = Lz4.decompress(data, length);
        }
        String serializeType = request.getHeader(NetConst.serialize_type);
        try {
            return SerializeType.codeOf(serializeType).getSerializer().deserializePollingData(data);
        } catch (Exception e) {
            throw new MyosotisException("Deserialize data fail", e);
        }
    }

}
