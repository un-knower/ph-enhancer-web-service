package org.ncpsb.phoenixcluster.enhancer.webservice.service;


import org.ncpsb.phoenixcluster.enhancer.webservice.dao.jpa.HBaseDao;
import org.ncpsb.phoenixcluster.enhancer.webservice.domain.ScoredPSM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by baimi on 2017/10/11.
 */


@Service
public class ScoredPSMService {
    private String scoredPSMTableName = "V_PXD000021_SCORED_PSM";
    private String clusterTableName = "V_CLUSTER";
    private String spectrumTableName = "V_CLUSTER_SPEC";

    HashMap<String, String> columnMap = new HashMap<String, String>() {{
        put("confidentScore","CONF_SC");
        put("clusterRatio","CLUSTER_RATIO");
        put("cluster_size","CLUSTER_SIZE");
    }};

    @Autowired
    private HBaseDao hBaseDao;


    public List<ScoredPSM> getScoredPSMs(Integer page, Integer size, String sortField, String sortDirection) {
        if (sortDirection == null) {
            sortDirection = "ASC";
        }

        StringBuffer querySql = new StringBuffer("SELECT * FROM " + scoredPSMTableName);
        if (sortField.equals("confidentScore") ||
                sortField.equals("clusterRatio") ||
                sortField.equals("clusterSize")
                ) {
            querySql.append(" ORDER BY " + columnMap.get(sortField) + " " + sortDirection + " ");
        }
        querySql.append(" LIMIT " + size);
        querySql.append(" OFFSET " + (page - 1) * size);

        System.out.println("Going to execute: " + querySql);
        List<ScoredPSM> scoredPSMs = (List<ScoredPSM>) hBaseDao.getScoredPSMs(querySql.toString(), null, new RowMapper<ScoredPSM>() {
            @Override
            public ScoredPSM mapRow(ResultSet rs, int rowNum) throws SQLException {
                ScoredPSM scoredPSM = new ScoredPSM();
                scoredPSM.setId(rs.getInt("ID"));
                scoredPSM.setPeptideSequence(rs.getString("PEP_SEQ"));

                scoredPSM.setClusterId(rs.getString("CLUSTER_ID"));
                scoredPSM.setClusterRatio(rs.getFloat("CLUSTER_RATIO"));
                scoredPSM.setClusterSize(rs.getInt("CLUSTER_SIZE"));

                scoredPSM.setConfidentScore(rs.getFloat("CONF_SC"));
                scoredPSM.setRecommendPeptide(rs.getString("RECOMMEND_PEP"));

                scoredPSM.setSpectraNum(rs.getInt("NUM_SPEC"));
                scoredPSM.setSpectraTitles(rs.getString("SPECTRA"));

                return scoredPSM;
            }
        });
        return (scoredPSMs != null && scoredPSMs.size() > 0) ? (List) scoredPSMs : null;
    }

    public ScoredPSM getPSMByTitle(String title, Object o) {
        StringBuffer querySql = new StringBuffer("SELECT * FROM compare_5_clusters WHERE ");
        querySql.append("\"ROW\" = '" + title + "'");

        ScoredPSM scoredPSM = (ScoredPSM) hBaseDao.getScoredPSM(querySql.toString(), null, new RowMapper<ScoredPSM>() {
            @Override
            public ScoredPSM mapRow(ResultSet rs, int rowNum) throws SQLException {
                ScoredPSM scoredPSM = new ScoredPSM();
                return scoredPSM;
            }
        });
        return scoredPSM;

    }

    public Integer totalScoredPSM() {
        StringBuffer querySql = new StringBuffer("SELECT COUNT(*) AS total FROM " + scoredPSMTableName);
        Integer totalElement = (Integer) hBaseDao.queryTotal(querySql.toString());
        return totalElement;
    }
}


