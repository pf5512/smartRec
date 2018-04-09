package com.thousandsunny.cms.domain.service;

import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.cms.model.Site;
import com.thousandsunny.cms.model.SiteRootChannels;
import com.thousandsunny.cms.domain.repository.SiteRepository;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.DocumentFile;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.DocumentFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.thousandsunny.cms.ModuleKey.SiteTemplateEnum.TEMP1;
import static com.thousandsunny.cms.ModuleKey.SiteTypeEnum;
import static com.thousandsunny.cms.ModuleKey.SiteTypeEnum.INFORMATION;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static java.util.stream.Collectors.toList;
@Service
public class SiteService extends BaseService<Site> {

    @Autowired
    private BaseChannelService baseChannelService;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private DocumentFileService documentFileService;

    public List<Site> findByEntityStatus() {
        return siteRepository.findByEntityStatusAndIsDelete(YES,NO);
    }

    public void saveSite(Site site, MultipartFile file) {
        if (site.getId() != null) {
            Site oldSite = siteRepository.findOne(site.getId());
            if (site.getType() == INFORMATION) {
                site.setTemplate(TEMP1);
            } else {
                oldSite.setTemplate(site.getTemplate());
            }
            DocumentFile attachPath;
            if (file != null) {
                attachPath = documentFileService.saveDocumentFile(file, ModuleKey.FileType.IMAGE);
                oldSite.setAttachPath(attachPath);
            }
            oldSite.setName(site.getName());
            oldSite.setType(site.getType());
            oldSite.setUrl(site.getUrl());
            oldSite.setDescription(site.getDescription());
            siteRepository.save(oldSite);
        } else {
            siteRepository.save(site);
        }
    }

    /**
     * 完整的栏目树
     */
    public List<SiteRootChannels> siteRootChannels() {
        return findAll().stream().map(site -> {
            List<Channel> channels = baseChannelService.siteRootChannel(site.getId());
            return new SiteRootChannels(site, channels);
        }).collect(toList());
    }

    public List<Site> findByTypeIn(List<SiteTypeEnum> siteType) {
        return siteRepository.findByIsDeleteAndTypeIn(NO,siteType);
    }
}
