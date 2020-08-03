package com.gitee.qdbp.jdbc.test.base;

import java.io.Serializable;
import javax.persistence.Id;

public class IdEntity implements Serializable {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
