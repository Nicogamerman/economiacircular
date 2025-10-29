package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.RecyclingCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RecyclingCenterRepositoryTest {

    @Autowired
    private RecyclingCenterRepository recyclingCenterRepository;

    private RecyclingCenter nycCenter;
    private RecyclingCenter laCenter;

    @BeforeEach
    void setUp() {
        recyclingCenterRepository.deleteAll();

        nycCenter = new RecyclingCenter(
                "NYC Center",
                "Near NYC",
                "New York, NY",
                40.7128,
                -74.0060,
                "555-0001",
                "nyc@center.com",
                RecyclingCenter.CenterType.RECYCLING_CENTER
        );
        nycCenter.setStatus(RecyclingCenter.CenterStatus.ACTIVE);

        laCenter = new RecyclingCenter(
                "LA Center",
                "Near LA",
                "Los Angeles, CA",
                34.0522,
                -118.2437,
                "555-0002",
                "la@center.com",
                RecyclingCenter.CenterType.NGO
        );
        laCenter.setStatus(RecyclingCenter.CenterStatus.ACTIVE);

        recyclingCenterRepository.save(nycCenter);
        recyclingCenterRepository.save(laCenter);
    }

    @Test
    @DisplayName("findByCenterTypeAndStatus returns only matching centers")
    void testFindByCenterTypeAndStatus() {
        List<RecyclingCenter> results = recyclingCenterRepository
                .findByCenterTypeAndStatus(
                        RecyclingCenter.CenterType.RECYCLING_CENTER,
                        RecyclingCenter.CenterStatus.ACTIVE
                );

        assertThat(results)
                .hasSize(1)
                .first()
                .extracting(RecyclingCenter::getName)
                .isEqualTo("NYC Center");
    }

    @Test
    @DisplayName("findNearbyCenters within 10km of NYC returns NYC center")
    void testFindNearbyCentersWithinRadius() {
        List<RecyclingCenter> results = recyclingCenterRepository.findNearbyCenters(40.7128, -74.0060, 10.0);

        assertThat(results)
                .extracting(RecyclingCenter::getName)
                .contains("NYC Center")
                .doesNotContain("LA Center");
    }

    @Test
    @DisplayName("findNearbyCenters far from NYC excludes LA center with small radius")
    void testFindNearbyCentersExcludesFarCenters() {
        List<RecyclingCenter> results = recyclingCenterRepository.findNearbyCenters(40.7128, -74.0060, 50.0);

        assertThat(results)
                .extracting(RecyclingCenter::getName)
                .containsOnly("NYC Center");
    }

    @Test
    @DisplayName("findNearbyCenters with null latitude returns all (bypass filter)")
    void testFindNearbyCentersNullLatitudeReturnsAll() {
        List<RecyclingCenter> results = recyclingCenterRepository.findNearbyCenters(null, -74.0, 1.0);
        assertThat(results)
                .extracting(RecyclingCenter::getName)
                .contains("NYC Center", "LA Center");
    }

    @Test
    @DisplayName("findNearbyCenters with null longitude returns all (bypass filter)")
    void testFindNearbyCentersNullLongitudeReturnsAll() {
        List<RecyclingCenter> results = recyclingCenterRepository.findNearbyCenters(40.7, null, 1.0);
        assertThat(results)
                .extracting(RecyclingCenter::getName)
                .contains("NYC Center", "LA Center");
    }
}


