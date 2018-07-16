package syedshahriar.com.KixHub.util;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;
import syedshahriar.com.KixHub.models.HitsObject;

public interface ElasticSearchAPI {

    @GET("_search")
    Call<HitsObject> search (
            @HeaderMap Map<String,String> headers,
            @Query("default_operator") String operator,
            @Query("q") String query
            );

}
