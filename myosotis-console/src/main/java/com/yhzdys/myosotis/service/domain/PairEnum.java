package com.yhzdys.myosotis.service.domain;

import com.yhzdys.myosotis.web.entity.vo.PairVO;

public interface PairEnum {

    String getCode();

    String getName();

    default PairVO toPair() {
        return new PairVO(this.getCode(), this.getName());
    }
}
