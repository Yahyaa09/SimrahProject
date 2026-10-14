import { useEffect, useState } from 'react';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const ROUTINE_API = `${API_BASE_URL}/api/v1/routine`;

const initialForm = {
  primaryConcern: 'Body Acne',
  severity: 'Mild',
  allergens: [],
  otherAllergies: '',
};

const toAllergyString = ({ allergens, otherAllergies }) =>
  [...allergens, otherAllergies].map((value) => value.trim()).filter(Boolean).join(', ');

async function readError(response) {
  try {
    const body = await response.json();
    if (body?.error) return body.error;
  } catch {
    // ignore non-JSON error bodies
  }
  return `Request failed with status ${response.status}`;
}

function AllergyPicker({ options, selected, other, onToggle, onOtherChange }) {
  return (
    <fieldset className="allergy-picker">
      <legend>Allergies &amp; sensitivities</legend>
      <p className="hint">Tap any that apply. We'll swap out products that contain them.</p>
      {options.length > 0 ? (
        <div className="chip-grid">
          {options.map((option) => {
            const active = selected.includes(option.key);
            return (
              <button
                type="button"
                key={option.key}
                className={`chip ${active ? 'active' : ''}`}
                aria-pressed={active}
                title={option.description}
                onClick={() => onToggle(option.key)}
              >
                {active ? '✓ ' : ''}
                {option.label}
              </button>
            );
          })}
        </div>
      ) : (
        <p className="hint">Common allergens couldn't be loaded. You can still type them below.</p>
      )}
      <input
        type="text"
        value={other}
        onChange={(event) => onOtherChange(event.target.value)}
        placeholder="Other ingredients, comma-separated (e.g. niacinamide, coconut)"
      />
    </fieldset>
  );
}

function RoutineSteps({ products }) {
  return (
    <>
      <ol className="step-flow" aria-label="Routine order">
        {products.map((product, index) => (
          <li key={product.id ?? `${product.name}-${index}`}>
            <span className="step-badge">
              Step {index + 1}: {product.category}
            </span>
            {index < products.length - 1 && <span className="step-arrow">→</span>}
          </li>
        ))}
      </ol>

      <div className="routine-list">
        {products.map((product, index) => (
          <div className="routine-item" key={product.id ?? `${product.name}-${index}`}>
            <span className="step-pill">Step {index + 1}</span>
            <div>
              <h3>{product.name}</h3>
              <p>
                {product.category}
                {product.activeIngredient && (
                  <span className="ingredient-tag">{product.activeIngredient}</span>
                )}
              </p>
              {product.ingredients && <p className="ingredients">Ingredients: {product.ingredients}</p>}
            </div>
          </div>
        ))}
      </div>
    </>
  );
}

function App() {
  const [mode, setMode] = useState('choose');
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);
  const [analysis, setAnalysis] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [allergenOptions, setAllergenOptions] = useState([]);

  const isResultsVisible = Boolean(result);
  const isSafe = result?.safetyCheck?.isSafe ?? result?.safetyCheck?.safe ?? false;
  const safetyMessage = result?.safetyCheck?.message ?? '';
  const routine = result?.orderedRoutine ?? [];
  const amRoutine = result?.amRoutine ?? routine;
  const pmRoutine = result?.pmRoutine ?? [];
  const adjustments = result?.adjustments ?? [];
  const allergyAdjustments = result?.allergyAdjustments ?? [];
  const sections =
    pmRoutine.length > 0
      ? [
          { title: '☀️ Morning (AM)', products: amRoutine },
          { title: '🌙 Evening (PM)', products: pmRoutine },
        ]
      : [{ title: '', products: amRoutine }];

  useEffect(() => {
    fetch(`${ROUTINE_API}/allergens`)
      .then((response) => (response.ok ? response.json() : []))
      .then(setAllergenOptions)
      .catch(() => setAllergenOptions([]));
  }, []);

  useEffect(() => {
    if (!imageFile) {
      setPreviewUrl('');
      return undefined;
    }
    const url = URL.createObjectURL(imageFile);
    setPreviewUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [imageFile]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const toggleAllergen = (key) => {
    setForm((current) => ({
      ...current,
      allergens: current.allergens.includes(key)
        ? current.allergens.filter((value) => value !== key)
        : [...current.allergens, key],
    }));
  };

  const allergyPicker = (
    <AllergyPicker
      options={allergenOptions}
      selected={form.allergens}
      other={form.otherAllergies}
      onToggle={toggleAllergen}
      onOtherChange={(value) => setForm((current) => ({ ...current, otherAllergies: value }))}
    />
  );

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch(`${ROUTINE_API}/build`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          primaryConcern: form.primaryConcern,
          severity: form.severity,
          allergies: toAllergyString(form),
        }),
      });

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      setAnalysis(null);
      setResult(await response.json());
    } catch (err) {
      setError(err instanceof TypeError ? 'Unable to reach the routine builder API.' : err.message);
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  const handleAnalyze = async (event) => {
    event.preventDefault();
    if (!imageFile) {
      setError('Please choose a photo first.');
      return;
    }
    setLoading(true);
    setError('');

    try {
      const body = new FormData();
      body.append('image', imageFile);
      body.append('allergies', toAllergyString(form));

      const response = await fetch(`${ROUTINE_API}/analyze-skin`, { method: 'POST', body });
      if (!response.ok) {
        throw new Error(await readError(response));
      }

      const data = await response.json();
      setAnalysis(data);
      setResult(data.routine);
    } catch (err) {
      setError(err instanceof TypeError ? 'Unable to reach the routine builder API.' : err.message);
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setMode('choose');
    setResult(null);
    setAnalysis(null);
    setImageFile(null);
    setError('');
    setLoading(false);
    setForm(initialForm);
  };

  return (
    <div className="app-shell">
      <div className="card">
        <div className="hero">
          <div className="hero-top">
            <p className="eyebrow">Skin routine builder</p>
            {(mode !== 'choose' || isResultsVisible) && (
              <button className="reset-button" type="button" onClick={reset}>
                ↺ Reset / Start Over
              </button>
            )}
          </div>
          <h1>Build a simple skincare routine in seconds.</h1>
          <p className="subtext">
            Take the quiz or upload a photo to get an ordered routine with an automatic Match &amp; Clash
            safety check.
          </p>
        </div>

        {mode === 'choose' && !isResultsVisible && (
          <div className="toggle-grid">
            <button className="choice-card" onClick={() => setMode('quiz')}>
              <span className="choice-icon">📝</span>
              <strong>Take the Skin Quiz</strong>
              <span>Answer 3 quick questions</span>
            </button>
            <button className="choice-card" onClick={() => setMode('photo')}>
              <span className="choice-icon">📷</span>
              <strong>Upload a Photo</strong>
              <span>Let the scanner detect your concern</span>
            </button>
          </div>
        )}

        {mode === 'photo' && !isResultsVisible && (
          <form className="photo-form" onSubmit={handleAnalyze}>
            <label className="dropzone">
              {previewUrl ? (
                <img src={previewUrl} alt="Selected skin preview" className="preview" />
              ) : (
                <>
                  <span className="choice-icon">📷</span>
                  <strong>Click to choose a photo</strong>
                  <span>JPG or PNG, up to 10MB</span>
                </>
              )}
              <input
                type="file"
                accept="image/png,image/jpeg"
                onChange={(event) => {
                  setError('');
                  setImageFile(event.target.files?.[0] ?? null);
                }}
              />
            </label>

            {imageFile && <p className="file-name">{imageFile.name}</p>}
            {allergyPicker}
            {error && <div className="error-banner">{error}</div>}

            <div className="button-row">
              <button className="primary-button" type="submit" disabled={loading || !imageFile}>
                {loading ? 'Scanning...' : 'Scan My Skin'}
              </button>
              <button className="ghost-button" type="button" onClick={reset}>
                Back
              </button>
            </div>
          </form>
        )}

        {mode === 'quiz' && !isResultsVisible && (
          <form className="quiz-form" onSubmit={handleSubmit}>
            <label>
              Primary Concern
              <select name="primaryConcern" value={form.primaryConcern} onChange={handleChange}>
                <option>Body Acne</option>
                <option>Hyperpigmentation</option>
                <option>Strawberry Skin</option>
              </select>
            </label>

            <label>
              Severity
              <select name="severity" value={form.severity} onChange={handleChange}>
                <option>Mild</option>
                <option>Moderate</option>
                <option>Severe</option>
              </select>
            </label>

            {allergyPicker}

            {error && <div className="error-banner">{error}</div>}

            <div className="button-row">
              <button className="primary-button" type="submit" disabled={loading}>
                {loading ? 'Generating...' : 'Generate Routine'}
              </button>
              <button className="ghost-button" type="button" onClick={reset}>
                Back
              </button>
            </div>
          </form>
        )}

        {isResultsVisible && (
          <div className="results">
            {analysis && (
              <div className="analysis-banner">
                <span>
                  Detected concern: <strong>{analysis.detectedConcern}</strong>
                </span>
                <span className="analysis-meta">
                  {Math.round(analysis.confidence * 100)}% confidence ·{' '}
                  {analysis.source === 'huggingface' ? 'AI model' : 'Demo scanner'}
                </span>
              </div>
            )}

            {allergyAdjustments.length > 0 && (
              <div className="safety-banner allergy" role="status">
                <span className="clash-icon">🛡️</span>
                <div>
                  <strong className="clash-title">Allergy-safe swaps</strong>
                  <ul>
                    {allergyAdjustments.map((note) => (
                      <li key={note}>{note}</li>
                    ))}
                  </ul>
                </div>
              </div>
            )}

            {!isSafe ? (
              <div className="safety-banner unsafe" role="alert">
                <span className="clash-icon">⚠️</span>
                <div>
                  <strong className="clash-title">Match &amp; Clash Warning</strong>
                  <p>{safetyMessage}</p>
                </div>
              </div>
            ) : adjustments.length > 0 ? (
              <div className="safety-banner adjusted" role="status">
                <span className="clash-icon">🔄</span>
                <div>
                  <strong className="clash-title">Routine adjusted for safety</strong>
                  <ul>
                    {adjustments.map((note) => (
                      <li key={note}>{note}</li>
                    ))}
                  </ul>
                </div>
              </div>
            ) : (
              <div className="safety-banner safe">✅ {safetyMessage}</div>
            )}

            <div className="results-header">
              <h2>Your routine</h2>
            </div>

            {routine.length === 0 ? (
              <p className="empty-state">No products found for this concern yet.</p>
            ) : (
              sections.map((section) => (
                <section className="routine-section" key={section.title}>
                  {section.title && <h3 className="section-title">{section.title}</h3>}
                  <RoutineSteps products={section.products} />
                </section>
              ))
            )}

            <button className="primary-button" type="button" onClick={reset}>
              ↺ Reset / Start Over
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
