
/**
 * Tencent is pleased to support the open source community by making MSEC available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the GNU General Public License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 *
 *     https://opensource.org/licenses/GPL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the 
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */


package beans.service;

import beans.request.DelSecondLevelServiceIPInfoRequest;
import beans.request.IPPortPair;
import beans.response.DelMachineResponse;
import beans.response.DelSecondLevelServiceIPInfoResponse;
import ngse.org.DBUtil;
import ngse.org.JsonRPCHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/27.
 * 删除标准服务的IP信息
 */
public class DelSecondLevelServiceIPInfo extends JsonRPCHandler {
    public DelSecondLevelServiceIPInfoResponse exec(DelSecondLevelServiceIPInfoRequest request)
    {
        DelSecondLevelServiceIPInfoResponse response = new DelSecondLevelServiceIPInfoResponse();
        response.setMessage("unkown error.");
        response.setStatus(100);
        String result = checkIdentity();
        if (!result.equals("success"))
        {
            response.setStatus(99);
            response.setMessage(result);
            return response;
        }
        if (request.getIpToDel() == null || request.getIpToDel().length == 0 )
        {
            response.setMessage("IP /port to be deleted should NOT be empty.");
            response.setStatus(100);
            return response;
        }
        DBUtil util = new DBUtil();
        if (util.getConnection() == null)
        {
            response.setMessage("DB connect failed.");
            response.setStatus(100);
            return response;
        }
        try {
            int total = 0;
            IPPortPair[] ipToDel = request.getIpToDel();
            for (int i = 0; i < ipToDel.length; i++) {
                String sql = "delete from t_second_level_service_ipinfo where ip=? and port=? and status='disabled'";
                List<Object> params = new ArrayList<Object>();
                params.add(ipToDel[i].getIp());
                params.add(ipToDel[i].getPort());
                try {
                    int delNum = util.updateByPreparedStatement(sql, params);
                    if (delNum > 0) {
                        total += delNum;
                        continue;
                    } else {
                        response.setMessage("delete record number is " + delNum);
                        response.setDelNumber(delNum);
                        response.setStatus(100);
                        return response;
                    }
                } catch (SQLException e) {
                    response.setMessage("Delete record failed:" + e.toString());
                    response.setStatus(100);
                    e.printStackTrace();
                    return response;
                }
            }
            response.setMessage("success");
            response.setDelNumber(total);
            response.setStatus(0);
            return response;
        }
        finally {
            util.releaseConn();
        }

    }
    public static String deleteAll(String flsn, String slsn)
    {
        DBUtil util = new DBUtil();
        if (util.getConnection() == null)
        {
           return "DB connect failed.";

        }
        String sql = "delete from t_second_level_service_ipinfo where " +
                "first_level_service_name=? and second_level_service_name=?";
        List<Object> params = new ArrayList<Object>();
        params.add(flsn);
        params.add(slsn);
        try {
            int delNum = util.updateByPreparedStatement(sql, params);
            return "success";
        }
        catch (SQLException e)
        {

            e.printStackTrace();
            return e.getMessage();
        }
        finally {
            util.releaseConn();
        }

    }

}
