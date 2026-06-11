import { Link } from "react-router-dom";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { Category, useApi } from "../../services/apiClient";
import { getCategories } from "./portalApi";

function CategoryBranch({ category }: { category: Category }) {
  return (
    <li>
      <Link to={`/search?categoryId=${category.id}`}>{category.name}</Link>
      {category.children.length > 0 && (
        <ul>
          {category.children.map((child) => (
            <CategoryBranch key={child.id} category={child} />
          ))}
        </ul>
      )}
    </li>
  );
}

export function CategoryBrowse() {
  const { data, error, loading } = useApi(getCategories, []);

  if (loading) return <LoadingPanel label="Loading categories" />;
  if (error) return <ErrorState error={error} />;
  if (!data || data.length === 0) return <EmptyState title="No categories are available" />;

  return (
    <div className="page-layout">
      <section>
        <h1>Categories</h1>
        <ul className="tree-list">
          {data.map((category) => (
            <CategoryBranch key={category.id} category={category} />
          ))}
        </ul>
      </section>
    </div>
  );
}
