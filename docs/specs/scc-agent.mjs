#!/usr/bin/env node
import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

const repoRoot = process.cwd();
const featureListPath = path.join(repoRoot, 'docs/specs/feature-list.json');
const allowedAgents = ['codex', 'claude'];
const allowedActions = ['plan', 'start', 'review', 'done', 'audit', 'sync'];

const actionLabels = {
  plan: '작업 계획 작성',
  start: '작업 시작 준비',
  review: 'PR 리뷰 연결',
  done: '구현 완료 처리',
  audit: 'SCC audit',
  sync: 'SCC tracking 동기화',
};

function parseArgs(argv) {
  const args = { _: [] };
  for (let index = 0; index < argv.length; index += 1) {
    const token = argv[index];
    if (!token.startsWith('--')) {
      args._.push(token);
      continue;
    }

    const key = token.slice(2);
    const next = argv[index + 1];
    if (!next || next.startsWith('--')) {
      args[key] = true;
      continue;
    }

    args[key] = next;
    index += 1;
  }
  return args;
}

function shellArg(value) {
  const text = String(value);
  if (/^[A-Za-z0-9_./:@-]+$/.test(text)) return text;
  return `'${text.replaceAll("'", "'\\''")}'`;
}

function readFeature(featureUid) {
  if (!featureUid || !fs.existsSync(featureListPath)) return null;
  const registry = JSON.parse(fs.readFileSync(featureListPath, 'utf8'));
  return registry.features.find((feature) => feature.uid === featureUid) || null;
}

function trackingCommand(action, args) {
  const cliAction = action === 'plan' ? 'plan-set' : action;
  const parts = ['node', 'docs/specs/scc-action.mjs', cliAction];
  if (args.feature) parts.push('--feature', args.feature);
  if (args.agent) parts.push('--agent', args.agent);
  if (args.pr) parts.push('--pr', args.pr);
  if (args.issue) parts.push('--issue', args.issue);
  if (args.branch) parts.push('--branch', args.branch);
  if (args.tests) parts.push('--tests', args.tests);
  if (args.screenshots) parts.push('--screenshots', args.screenshots);
  if (args.reason) parts.push('--reason', args.reason);
  if (args.notes) parts.push('--notes', args.notes);
  if (action === 'plan') {
    parts.push('--status', args.status || 'draft');
    parts.push('--summary', args.summary || '<작업 계획 요약>');
    parts.push('--tasks', args.tasks || '<작업 목록을 쉼표로 구분>');
    parts.push('--files', args.files || '<예상 수정 파일을 쉼표로 구분>');
    parts.push('--tests', args.tests || '<테스트 계획을 쉼표로 구분>');
    parts.push('--risks', args.risks || '<리스크를 쉼표로 구분>');
  }
  return parts.map(shellArg).join(' ');
}

function missingValueGuide(action, args) {
  const missing = [];
  if (action === 'review' && !args.pr) missing.push('PR 번호 또는 PR URL');
  if (!missing.length) return [];
  return [
    `아래 값이 아직 없으면 명령을 실행하기 전에 사용자에게 먼저 확인해줘: ${missing.join(', ')}`,
    '필수 값이 확인되기 전에는 SCC 명령을 실행하지 마.',
    '값을 확인한 뒤에는 아래 명령에 필요한 옵션을 채워서 실행해줘.',
  ];
}

function featureSummary(feature) {
  if (!feature) return [];
  return [
    `Feature 제목: ${feature.title}`,
    `화면군: ${feature.classification.area} / ${feature.classification.subarea}`,
    `Trigger: ${feature.behavior.trigger}`,
    `Response: ${feature.behavior.response}`,
    `원천 스펙: docs/specs/${feature.links.specPath}:${feature.links.sourceLine}`,
  ];
}

function buildPrompt(action, args) {
  const feature = readFeature(args.feature);
  const command = trackingCommand(action, args);
  const auditCommand = 'node docs/specs/scc-action.mjs audit';
  const agentGuide = args.agent === 'codex'
    ? '`linkit-spec` 스킬을 사용해서'
    : 'Claude용 LinkIt SCC 작업 절차에 따라';

  if (action === 'start') {
    return [
      'LinkIt KMP의 SCC 정책 문서를 확인한 뒤 작업 시작 준비를 해줘.',
      `대상 Feature: ${args.feature}`,
      ...featureSummary(feature),
      '중요: 아직 개발을 바로 시작하지 마. 코드 수정, 커밋, SCC 상태 변경도 하지 마.',
      '먼저 아래 내용을 한국어로 정리해줘.',
      '1. Feature 스펙, 연결 이미지, TBD, 관련 문서에서 확인해야 할 내용',
      '2. 구현 범위와 영향을 받을 가능성이 높은 파일/모듈',
      '3. 필요한 작업 목록을 작은 체크리스트로 분해한 내용',
      '4. 필요한 테스트와 스크린샷 검증 항목',
      '5. 진행 전에 사용자 확인이 필요한 질문 또는 리스크',
      '정리 마지막에는 "이 작업 목록으로 진행할까요?"라고 묻고 사용자 답변을 기다려.',
      '사용자가 진행을 승인한 뒤에만 아래 SCC start 명령을 실행하고 audit을 돌린 다음 개발을 시작해.',
      'start 명령은 현재 등록된 GitHub ID를 developer로 자동 저장한다.',
      command,
      `승인 후 실행할 audit 명령: ${auditCommand}`,
    ].join('\n');
  }

  if (action === 'plan') {
    return [
      'LinkIt KMP의 SCC 정책 문서를 확인한 뒤 Feature 작업 계획을 작성해줘.',
      `대상 Feature: ${args.feature}`,
      ...featureSummary(feature),
      '아직 코드를 수정하거나 SCC 상태를 start로 바꾸지 마.',
      '먼저 Feature 스펙, 연결 이미지, TBD, 관련 Feature를 확인한 뒤 아래 형식으로 한국어 작업 계획을 제안해줘.',
      '1. 작업 목표 요약',
      '2. 세부 작업 체크리스트',
      '3. 영향을 받을 가능성이 높은 파일/모듈',
      '4. 테스트 및 스크린샷 검증 계획',
      '5. 리스크와 사용자 확인 질문',
      '마지막에는 "이 계획을 SCC에 저장할까요?"라고 묻고 사용자 답변을 기다려.',
      '사용자가 저장을 승인하면 아래 plan-set 명령의 placeholder를 실제 계획 내용으로 채워 실행하고 audit을 돌려줘.',
      command,
      `저장 후 실행할 audit 명령: ${auditCommand}`,
    ].join('\n');
  }

  return [
    'LinkIt KMP의 SCC 정책 문서를 확인한 뒤 아래 작업을 진행해줘.',
    `대상 Feature: ${args.feature || '-'}`,
    ...featureSummary(feature),
    `작업: ${actionLabels[action] || action}`,
    ...missingValueGuide(action, args),
    `${agentGuide} 다음 SCC 명령을 실행해줘.`,
    command,
    `명령 실행 후 \`${auditCommand}\`를 실행하고, 변경된 상태와 경고가 있으면 한국어로 요약해줘.`,
  ].join('\n');
}

function logPrompt(action, args, prompt) {
  if (!args.feature || ['audit', 'sync'].includes(action)) return;
  const result = spawnSync(
    process.execPath,
    [
      'docs/specs/scc-action.mjs',
      'prompt-log',
      '--feature',
      args.feature,
      '--agent',
      args.agent,
      '--prompt-action',
      action,
      '--prompt',
      prompt,
      '--source',
      'scc-agent',
    ],
    {
      cwd: repoRoot,
      encoding: 'utf8',
    },
  );
  if (result.status !== 0) {
    throw new Error(`프롬프트 기록 실패: ${result.stderr || result.stdout}`);
  }
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const action = args._[0];
  const agent = args.agent || 'codex';

  if (!allowedActions.includes(action)) {
    throw new Error(`알 수 없는 action입니다: ${action || '(없음)'}`);
  }
  if (!allowedAgents.includes(agent)) {
    throw new Error(`알 수 없는 agent입니다: ${agent}. 허용값: ${allowedAgents.join(', ')}`);
  }
  if (!['audit', 'sync'].includes(action) && !args.feature) {
    throw new Error('Feature action에는 --feature <featureUid>가 필요합니다.');
  }

  args.agent = agent;
  const prompt = buildPrompt(action, args);

  if (args['print-prompt']) {
    console.log(prompt);
    return;
  }

  logPrompt(action, args, prompt);

  const result = spawnSync(agent, [prompt], {
    cwd: repoRoot,
    env: process.env,
    stdio: 'inherit',
  });

  if (result.error) throw result.error;
  process.exitCode = result.status ?? 0;
}

try {
  main();
} catch (error) {
  console.error(error.message);
  process.exitCode = 1;
}
