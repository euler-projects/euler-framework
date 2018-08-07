package net.eulerframework.web.module.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.eulerframework.web.module.file.entity.ArchivedFile;

/**
 * @author cFrost
 *
 */
@Repository
public interface ArchivedFileRepository extends JpaRepository<ArchivedFile, String> {

    /**
     * @param archivedFileId
     * @return
     */
    ArchivedFile findArchivedFileById(String archivedFileId);

}
