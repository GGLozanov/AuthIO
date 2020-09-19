package com.example.authio.models;

import com.example.authio.persistence.BaseEntity;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CacheModel<T extends BaseEntity> extends Model {
    @Expose
    protected T entity;

    public CacheModel(T entity) {
        this.entity = entity;
    }

    public CacheModel(String response, T entity) {
        super(response);
        this.entity = entity;
    }

    public CacheModel(String response) {
        super(response);
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}
