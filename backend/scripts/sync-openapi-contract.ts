import { copyFileSync, existsSync, mkdirSync } from "node:fs";
import { dirname, resolve } from "node:path";

const source = resolve(process.cwd(), "../.aiops-spec/contracts/openapi.yaml");
const fallbackSource = resolve(process.cwd(), ".aiops-spec/contracts/openapi.yaml");
const target = resolve(process.cwd(), "contracts/openapi.yaml");
const contractSource = existsSync(source) ? source : fallbackSource;

if (!existsSync(contractSource)) {
  if (existsSync(target)) {
    console.info(`OpenAPI contract already exists at ${target}`);
    process.exit(0);
  }
  throw new Error("OpenAPI source contract was not found under .aiops-spec/contracts/openapi.yaml.");
}

mkdirSync(dirname(target), { recursive: true });
copyFileSync(contractSource, target);
console.info(`Synced OpenAPI contract to ${target}`);
