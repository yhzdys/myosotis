package com.yhzdys.myosotis.database.mapper;

import com.yhzdys.myosotis.database.object.MyosotisSessionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyosotisSessionMapper {

    int insert(MyosotisSessionDO record);

    MyosotisSessionDO selectByUsername(String username);

    MyosotisSessionDO selectBySessionKey(String sessionKey);

    int update(MyosotisSessionDO record);

    int deleteById(Long id);

    int deleteByUsername(String username);
}
