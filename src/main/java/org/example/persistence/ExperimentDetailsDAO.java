package org.example.persistence;

import org.example.model.ExperimentDetails;
import org.example.model.UploadedFile;
import org.example.persistence.mapper.ExperimentDetailsMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

import javax.inject.Singleton;

@UseStringTemplate3StatementLocator
public interface ExperimentDetailsDAO {

    @SqlUpdate("UPDATE experiment_details SET attachment = :fileData, attachmentName = :fileName WHERE experiment_ID = :experimentId")
    int setAttachment(@BindBean UploadedFile file);

    @SqlQuery("SELECT experiment_ID, attachment, attachmentName FROM experiment_details WHERE experiment_ID = :experimentId")
    @Mapper(AttachmentMapper.class)
    UploadedFile getAttachment(@Bind("experimentId") int experimentId);

    @SqlQuery("SELECT * FROM experiment_details WHERE experiment_ID = :id")
    @Mapper(ExperimentDetailsMapper.class)
    ExperimentDetails find(@Bind("id") int id);

    @SqlUpdate("UPDATE experiment_details SET " +
            "netwerk = :netwerk," +
            "status = :status," +
            "kosten_innovatie = :kosten_innovatie," +
            "kosten_anders = :kosten_anders," +
            "doorlooptijd = :doorlooptijd," +
            "overige_opmerkingen = :overige_opmerkingen," +
            "archief_type = :archief_type" +
            " WHERE experiment_ID = :id")
    void updateExperimentDetails(@Bind("id") int id, @BindBean ExperimentDetails experimentDetails);

    @SqlUpdate("INSERT INTO experiment_details " +
            "(experiment_ID, netwerk, status, kosten_innovatie, kosten_anders, doorlooptijd, overige_opmerkingen, archief_type) " +
            "VALUES " +
            "(:experimentId, :netwerk, :status, :kosten_innovatie, :kosten_anders, :doorlooptijd, :overige_opmerkingen, :archief_type) ")
    int addExperimentDetails( @BindBean ExperimentDetails experimentDetails);

    @SqlUpdate("DELETE FROM experiment_details WHERE experiment_id = :id")
    void deleteExperimentDetails(@Bind("id") int id);

    void close();

}
