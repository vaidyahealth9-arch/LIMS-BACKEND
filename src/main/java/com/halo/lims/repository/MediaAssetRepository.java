package com.halo.lims.repository;

import com.halo.lims.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Integer> {
    Optional<MediaAsset> findByUrl(String url);
}
