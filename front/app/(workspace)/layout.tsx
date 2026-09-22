import { Suspense, type ReactNode } from "react";
import WorkspaceShell from "../components/workspace/WorkspaceShell";

export default function WorkspaceLayout({ children }: { children: ReactNode }) {
  return (
    <Suspense fallback={null}>
      <WorkspaceShell>{children}</WorkspaceShell>
    </Suspense>
  );
}
