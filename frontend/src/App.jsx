import { useMemo, useState } from 'react';

const initialForm = {
  primaryConcern: 'Body Acne',
  severity: 'Mild',
  allergies: '',
};

function App() {
  const [mode, setMode] = useState('choose');
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState(null);

  const isResultsVisible = useMemo(() => Boolean(result), [result]);
  const isSafe = result?.safetyCheck?.isSafe ?? result?.safetyCheck?.safe ?? false;
  const safetyMessage = result?.safetyCheck?.message ?? '';

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/api/v1/routine/build', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(form),
      });

      if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`);
      }

      const data = await response.json();
      setResult(data);
    } catch (err) {
      setError('Unable to reach the routine builder API.');
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setMode('choose');
    setResult(null);
    setError('');
    setLoading(false);
    setForm(initialForm);
  };

  return (
    <div className="app-shell">
      <div className="card">
        <div className="hero">
          <p className="eyebrow">Skin routine builder</p>
          <h1>Build a simple skincare routine in seconds.</h1>
          <p className="subtext">
            Choose the quiz flow to get an ordered routine from your Spring Boot backend.
          </p>
        </div>

        {mode === 'choose' && !isResultsVisible && (
          <div className="toggle-grid">
            <button className="primary-button" onClick={() => setMode('quiz')}>
              Take the Skin Quiz
            </button>
            <button className="secondary-button" onClick={() => setMode('photo')}>
              Upload a Photo
            </button>
          </div>
        )}

        {mode === 'photo' && !isResultsVisible && (
          <div className="photo-placeholder">
            <h2>Photo upload is coming soon</h2>
            <p>For now, use the skin quiz to generate a routine.</p>
            <button className="secondary-button" onClick={reset}>
              Back
            </button>
          </div>
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

            <label>
              Allergies
              <input
                name="allergies"
                type="text"
                value={form.allergies}
                onChange={handleChange}
                placeholder="List any ingredients or sensitivities"
              />
            </label>

            {error && <div className="error-banner">{error}</div>}

            <div className="button-row">
              <button className="primary-button" type="submit" disabled={loading}>
                {loading ? 'Generating...' : 'Generate Routine'}
              </button>
              <button className="ghost-button" type="button" onClick={reset}>
                Reset
              </button>
            </div>
          </form>
        )}

        {isResultsVisible && (
          <div className="results">
            <div className={`safety-banner ${isSafe ? 'safe' : 'unsafe'}`}>
              {safetyMessage}
            </div>

            <div className="results-header">
              <h2>Your routine</h2>
              <button className="ghost-button" type="button" onClick={reset}>
                Start over
              </button>
            </div>

            <div className="routine-list">
              {result?.orderedRoutine?.map((product, index) => (
                <div className="routine-item" key={product.id ?? `${product.name}-${index}`}>
                  <span className="step-pill">Step {index + 1}</span>
                  <div>
                    <h3>{product.name}</h3>
                    <p>{product.category}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
