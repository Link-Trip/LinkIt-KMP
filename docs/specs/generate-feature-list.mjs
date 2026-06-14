import fs from 'node:fs';
import path from 'node:path';

const specsDir = path.resolve('docs/specs');
const schemaVersion = 'spec-command-center.feature-list.v1';

const specInputs = [
  { slug: 'onboarding', file: 'onboarding-screen.md', title: '온보딩/최초진입', area: 'Onboarding', figmaSection: '15091:142439', accent: '#0f9f8f' },
  { slug: 'main', file: 'main-screen.md', title: '메인 지도', area: 'Main Map', figmaSection: '15091:144859', accent: '#2563eb' },
  { slug: 'explore', file: 'explore-feed-screen.md', title: '탐색 & 피드', area: 'Explore', figmaSection: '15091:151607', accent: '#7c3aed' },
  { slug: 'mypage', file: 'mypage-screen.md', title: '마이페이지', area: 'MyPage', figmaSection: '15091:151966', accent: '#ea580c' },
  { slug: 'library', file: 'library-screen.md', title: '보관함', area: 'Library', figmaSection: '15091:152408', accent: '#16a34a' },
];

const typeMatchers = [
  ['map_interaction', /MAP_|MARKER|지도|마커|위치|줌|GPS|현재 위치|위성/],
  ['navigation', /NAV|OPEN|이동|진입|더보기|탭|상세|보러가기|보기/],
  ['input', /INPUT|입력|붙여넣기|링크|폴더명|검색어/],
  ['validation', /INVALID|DUPLICATE|검증|오류|유효하지|중복/],
  ['modal_dialog', /CONFIRM|팝업|초기화|삭제|닫기|취소/],
  ['filter_sort', /FILTER|SORT|필터|정렬|국가|테마|비용/],
  ['async_process', /ANALYSIS|LOADING|COMPLETE|생성 중|분석|완료/],
  ['selection', /SELECT|선택|타입|옵션/],
  ['variant_experiment', /VARIANT|A\/B|대체안|후보/],
  ['display_state', /SHOW|INITIAL|GUIDE|노출|표시|상태|안내/],
];

function splitRow(line) {
  return line.trim().replace(/^\|/, '').replace(/\|$/, '').split('|').map((value) => value.trim());
}

function stripCode(value) {
  return value.replace(/^`|`$/g, '').trim();
}

function parseImageRefs(screenSlug, value) {
  return value
    .split(',')
    .map((item) => item.trim())
    .filter((item) => /^IMG-\d+$/i.test(item))
    .map((imageId) => `${screenSlug}:${imageId.toUpperCase()}`);
}

function findSpecVersion(markdown) {
  return (markdown.match(/스펙 버전:\s*`([^`]+)`/) || [null, ''])[1];
}

function resolveSpecStatus(rawStatus) {
  if (/대체안|후보/.test(rawStatus)) return 'variant';
  if (/정책 필요/.test(rawStatus) && /확정|sitemap|메모/.test(rawStatus)) return 'partial';
  if (/정책 필요|미정|확인 필요/.test(rawStatus)) return 'needs_policy';
  if (/추론/.test(rawStatus)) return 'inferred';
  if (/확정|sitemap|description|메모/.test(rawStatus)) return 'confirmed';
  return 'partial';
}

function resolveEvidenceLevel(rawStatus) {
  if (/sitemap/.test(rawStatus) && /메모|확정|description/.test(rawStatus)) return 'mixed';
  if (/sitemap/.test(rawStatus)) return 'sitemap_based';
  if (/메모/.test(rawStatus)) return 'memo_based';
  if (/추론/.test(rawStatus)) return 'agent_inferred';
  if (/확정|description/.test(rawStatus)) return 'figma_confirmed';
  return 'mixed';
}

function resolveDeliveryStatus(specStatus, tbdRefs) {
  if (specStatus === 'needs_policy' || specStatus === 'variant') return 'blocked';
  if (specStatus === 'partial' && tbdRefs.length > 0) return 'blocked';
  if (specStatus === 'inferred') return 'not_started';
  return 'ready';
}

function resolveFeatureType(featureId, title, trigger, response) {
  const text = `${featureId} ${title} ${trigger} ${response}`;
  return (typeMatchers.find(([, matcher]) => matcher.test(text)) || ['display_state'])[0];
}

function resolveSubarea(screenSlug, featureId, title, response) {
  const text = `${featureId} ${title} ${response}`;
  const maps = {
    main: [
      ['map', /MAP_|지도|위치|줌/],
      ['bottom_sheet', /SHEET|바텀시트|필터/],
      ['schedule_marker', /SCHEDULE_MARKER|일정마커/],
      ['place_marker', /PLACE_|장소/],
      ['create_schedule', /CREATE|CLIPBOARD|VIDEO_LINK|일정 생성|링크/],
    ],
    onboarding: [
      ['intro', /INTRO|인트로|최초/],
      ['guide', /GUIDE|안내/],
      ['video_link', /VIDEO|CLIPBOARD|LINK|추천 영상|링크/],
      ['analysis', /ANALYSIS|분석|생성 중|완료/],
      ['save', /SAVE|저장|삭제/],
      ['trip_detail', /TRIP_DETAIL|상세|요약|일정 리스트/],
    ],
    explore: [
      ['country', /COUNTRY|국가|권역|일본/],
      ['theme', /THEME|테마/],
      ['creator', /CREATOR|크리에이터|유튜버/],
      ['video_feed', /VIDEO|영상/],
      ['bottom_tab', /BOTTOM|하단/],
    ],
    library: [
      ['folder_grid', /LIBRARY_OPEN|폴더|그리드/],
      ['search', /SEARCH|검색/],
      ['filter_sort', /FILTER|SORT|필터|정렬/],
      ['add_menu', /ADD_MENU|직접 추가|저장 항목/],
      ['folder_input', /FOLDER|폴더명/],
      ['detail', /DETAIL|상세/],
      ['variant', /VARIANT|B안|지도 포함/],
    ],
    mypage: [
      ['map_setting', /MAP_TYPE|지도/],
      ['support', /FEEDBACK|BUG|피드백|버그/],
      ['terms', /TERMS|약관/],
      ['reset', /RESET|초기화/],
    ],
  };
  return (maps[screenSlug].find(([, matcher]) => matcher.test(text)) || ['general'])[0];
}

function resolveOwner(feature) {
  if (feature.status.spec === 'needs_policy' || feature.status.spec === 'variant') return 'PM';
  if (feature.classification.type === 'map_interaction') return 'KMP';
  if (['validation', 'input', 'async_process'].includes(feature.classification.type)) return 'KMP';
  if (feature.classification.type === 'modal_dialog') return 'Design';
  return 'Unassigned';
}

function linkTbds(screenSlug, feature) {
  const text = `${feature.featureId} ${feature.title} ${feature.behavior.trigger} ${feature.behavior.response}`;
  const links = [];
  const add = (tbdId) => links.push(`${screenSlug}:${tbdId}`);

  if (screenSlug === 'main') {
    if (/최소|바텀시트/.test(text)) add('TBD-01');
    if (/줌|배율/.test(text)) add('TBD-02');
    if (/클러스터|겹/.test(text)) add('TBD-03');
    if (/애니메이션|전환/.test(text)) add('TBD-04');
    if (/보관함에서/.test(text)) add('TBD-05');
    if (/직접 만들기/.test(text)) add('TBD-06');
    if (/뒤로|지도선택|빈 영역/.test(text)) add('TBD-07');
    if (/위치 권한|권한|서울|한국/.test(text)) add('TBD-08');
    if (/상세페이지|마커\/영역/.test(text)) add('TBD-09');
    if (/CTA|라벨|일정에서 보기|지도에서 보기/.test(text)) add('TBD-10');
  }

  if (screenSlug === 'onboarding') {
    if (/건너뛰기/.test(text)) add('TBD-01');
    if (/인트로|길이/.test(text)) add('TBD-02');
    if (/위치 권한|서울|한국/.test(text)) add('TBD-03');
    if (/분석|생성 중|완료/.test(text)) add('TBD-04');
    if (/보관함에서 가져오기|직접 만들기|생성 방식/.test(text)) add('TBD-05');
    if (/저장 폴더|전체 일정/.test(text)) add('TBD-06');
    if (/삭제/.test(text)) add('TBD-07');
  }

  if (screenSlug === 'explore') {
    if (/영상|유튜브|재생/.test(text)) add('TBD-01');
    if (/국가|권역/.test(text)) add('TBD-02');
    if (/Follow|크리에이터/.test(text)) add('TBD-03');
    if (/더보기|추천 여행 유튜버/.test(text)) add('TBD-04');
    if (/하단|보관함|지도/.test(text)) add('TBD-05');
  }

  if (screenSlug === 'library') {
    if (/A안|B안|지도 포함|대체안/.test(text)) add('TBD-01');
    if (/항목 타입|타입|목록/.test(text)) add('TBD-02');
    if (/정렬/.test(text)) add('TBD-03');
    if (/상세/.test(text)) add('TBD-04');
    if (/폴더명|저장 조건/.test(text)) add('TBD-05');
    if (/placeholder|토지주소|내용 입력|n\/n|수원|숙소/.test(text)) add('TBD-06');
    if (/필터/.test(text)) add('TBD-07');
  }

  if (screenSlug === 'mypage') {
    if (/지도|위성|기본/.test(text)) add('TBD-01');
    if (/피드백|버그/.test(text)) add('TBD-02');
    if (/초기화|삭제/.test(text)) add('TBD-03');
  }

  return [...new Set(links)];
}

function buildRegistry() {
  const specDocuments = [];
  const imageAssets = [];
  const tbdItems = [];
  const features = [];

  for (const specInput of specInputs) {
    const markdown = fs.readFileSync(path.join(specsDir, specInput.file), 'utf8');
    const lines = markdown.split(/\r?\n/);
    const specVersion = findSpecVersion(markdown);

    specDocuments.push({
      slug: specInput.slug,
      title: specInput.title,
      area: specInput.area,
      path: specInput.file,
      specVersion,
      figmaSection: specInput.figmaSection,
      accent: specInput.accent,
      reviewStatus: specInput.slug === 'main' ? 'Pass' : 'Pass with Comments',
    });

    for (let index = 0; index < lines.length; index += 1) {
      const line = lines[index];
      if (!/^\|\s*`?[^|`]+`?\s*\|/.test(line)) continue;

      const cells = splitRow(line);
      const id = stripCode(cells[0] || '');

      if (/^IMG-/.test(id)) {
        const absolutePath = stripCode(cells[1] || '');
        const relativePath = absolutePath.includes('/docs/specs/')
          ? absolutePath.split('/docs/specs/')[1]
          : absolutePath;

        imageAssets.push({
          uid: `${specInput.slug}:${id}`,
          imageId: id,
          screenSlug: specInput.slug,
          path: relativePath,
          description: cells[2] || '',
          source: { path: specInput.file, line: index + 1 },
        });
        continue;
      }

      if (/^TBD-/.test(id)) {
        tbdItems.push({
          uid: `${specInput.slug}:${id}`,
          tbdId: id,
          screenSlug: specInput.slug,
          title: cells[1] || '',
          description: cells[2] || '',
          status: 'open',
          owner: 'PM',
          impact: { featureRefs: [] },
          source: { path: specInput.file, line: index + 1 },
        });
        continue;
      }

      if (/^v\d/.test(id) || /^\d+$/.test(id) || !/^[A-Z0-9_]+$/.test(id)) continue;

      const rawStatus = cells[4] || '';
      const specStatus = resolveSpecStatus(rawStatus);
      const classification = {
        screenSlug: specInput.slug,
        area: specInput.area,
        subarea: resolveSubarea(specInput.slug, id, cells[1] || '', cells[3] || ''),
        type: resolveFeatureType(id, cells[1] || '', cells[2] || '', cells[3] || ''),
        tags: [],
      };

      const feature = {
        uid: `${specInput.slug}:${id}`,
        featureId: id,
        title: cells[1] || '',
        behavior: {
          trigger: cells[2] || '',
          response: cells[3] || '',
        },
        classification,
        status: {
          spec: specStatus,
          evidence: resolveEvidenceLevel(rawStatus),
          delivery: 'not_started',
          raw: rawStatus,
        },
        ownership: {
          owner: 'Unassigned',
          issue: null,
          pr: null,
          branch: null,
        },
        verification: {
          testStatus: 'not_started',
          screenshotStatus: 'not_started',
          notes: '',
        },
        links: {
          imageRefs: parseImageRefs(specInput.slug, cells[5] || ''),
          tbdRefs: [],
          dependsOn: [],
          relatedFeatureRefs: [],
          specPath: specInput.file,
          sourceLine: index + 1,
          figmaSection: specInput.figmaSection,
          specVersion,
        },
      };

      feature.links.tbdRefs = linkTbds(specInput.slug, feature);
      feature.status.delivery = resolveDeliveryStatus(specStatus, feature.links.tbdRefs);
      feature.ownership.owner = resolveOwner(feature);
      feature.classification.tags = [
        feature.classification.screenSlug,
        feature.classification.subarea,
        feature.classification.type,
        feature.status.spec,
        feature.status.delivery,
        feature.status.evidence,
      ];

      features.push(feature);
    }
  }

  for (const tbd of tbdItems) {
    tbd.impact.featureRefs = features.filter((feature) => feature.links.tbdRefs.includes(tbd.uid)).map((feature) => feature.uid);
  }

  for (const feature of features) {
    feature.links.relatedFeatureRefs = features
      .filter((candidate) => candidate.uid !== feature.uid && candidate.classification.screenSlug === feature.classification.screenSlug)
      .filter((candidate) => (
        candidate.classification.subarea === feature.classification.subarea
        || candidate.links.tbdRefs.some((tbdRef) => feature.links.tbdRefs.includes(tbdRef))
        || candidate.links.imageRefs.some((imageRef) => feature.links.imageRefs.includes(imageRef))
      ))
      .slice(0, 8)
      .map((candidate) => candidate.uid);
  }

  return {
    schemaVersion,
    generatedAt: new Date().toISOString(),
    project: {
      name: 'Pingo',
      figmaFile: 'Pingo v3.0.3',
      specRoot: 'docs/specs',
    },
    enums: {
      specStatus: ['confirmed', 'partial', 'needs_policy', 'inferred', 'variant', 'out_of_scope'],
      evidenceLevel: ['figma_confirmed', 'sitemap_based', 'memo_based', 'agent_inferred', 'mixed'],
      deliveryStatus: ['not_started', 'ready', 'in_progress', 'in_review', 'implemented', 'verified', 'blocked', 'deferred'],
      featureType: typeMatchers.map(([type]) => type),
      owner: ['Unassigned', 'PM', 'Design', 'KMP', 'QA'],
    },
    specDocuments,
    imageAssets,
    tbdItems,
    features,
  };
}

const registry = buildRegistry();
const jsonPath = path.join(specsDir, 'feature-list.json');
const jsPath = path.join(specsDir, 'feature-list.js');

fs.writeFileSync(jsonPath, `${JSON.stringify(registry, null, 2)}\n`);
fs.writeFileSync(
  jsPath,
  `// Generated by docs/specs/generate-feature-list.mjs.\nwindow.SPEC_COMMAND_CENTER_FEATURE_LIST = ${JSON.stringify(registry, null, 2)};\n`,
);

console.log(`Feature List generated: ${registry.features.length} features, ${registry.tbdItems.length} TBDs, ${registry.imageAssets.length} images`);
