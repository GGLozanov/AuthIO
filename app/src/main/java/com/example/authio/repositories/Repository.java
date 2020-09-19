package com.example.authio.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.authio.api.APIClient;
import com.example.authio.api.APIOperations;
import com.example.authio.models.Model;

public abstract class Repository {
    protected APIOperations API_OPERATIONS = APIClient
            .getAPIClient()
            .create(APIOperations.class); // create new instance of APIOperations through a retrofit instance to receive HTTP responses
}
