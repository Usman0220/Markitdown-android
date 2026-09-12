package com.example.sample

import com.example.data.model.ConversionDocument

object SampleDocuments {

    fun getSamples(): List<ConversionDocument> {
        return listOf(
            ConversionDocument(
                id = -1,
                title = "Q3 Financial Performance Report",
                sourceFileName = "financial_q3_report.xlsx",
                sourceFormat = "XLSX",
                fileSizeBytes = 48250,
                markdownContent = """
# Financial Performance Report (Q3)

## Sheet: Revenue Breakdown

| Region | Q1 Revenue | Q2 Revenue | Q3 Revenue | YoY Growth | Status |
| --- | --- | --- | --- | --- | --- |
| North America | $14,250,000 | $15,800,000 | $17,450,000 | +22.4% | Outperforming |
| EMEA | $8,900,000 | $9,450,000 | $10,120,000 | +13.7% | On Target |
| Asia Pacific | $5,600,000 | $6,200,000 | $7,350,000 | +31.2% | Rapid Growth |
| Latin America | $2,100,000 | $2,300,000 | $2,680,000 | +27.6% | Strong Growth |
| **Total Global** | **$30,850,000** | **$33,750,000** | **$37,600,000** | **+21.8%** | **Target Met** |

## Sheet: Key Metrics & Ratios

| Financial Metric | Target | Actual (Q3) | Variance | Health |
| --- | --- | --- | --- | --- |
| Gross Margin | 68.0% | 71.4% | +3.4% | Exceptional |
| Operating Margin | 24.0% | 26.8% | +2.8% | Strong |
| Net Retention Rate | 115% | 122% | +7.0% | Best in Class |
| Customer Acquisition Cost | $4,500 | $3,850 | -$650 | Efficient |

### Executive Takeaway
> "Q3 marked the highest quarterly operating efficiency in company history. Expansion in the enterprise cloud segment contributed 64% of total net new ARR."
                """.trimIndent(),
                wordCount = 210,
                charCount = 1350,
                readingTimeMinutes = 1,
                timestamp = System.currentTimeMillis() - 86400000 * 2,
                isFavorite = true,
                tags = "finance, spreadsheet, quarterly"
            ),

            ConversionDocument(
                id = -2,
                title = "AI Architecture Specification",
                sourceFileName = "system_architecture_spec.docx",
                sourceFormat = "DOCX",
                fileSizeBytes = 112400,
                markdownContent = """
# System Architecture Specification

## 1. Overview
This document specifies the distributed pipeline architecture for the **Neural Semantic Indexer**. The system processes unstructured multi-modal enterprise data into vectorized semantic representations.

### 1.1 Key Objectives
- **High Throughput**: Capable of indexing up to 50,000 documents per minute.
- **Strict Isolation**: Tenant sandboxing with cryptographic namespace partitioning.
- **Zero Data Loss**: Append-only transactional write-ahead logging.

## 2. Ingestion Pipeline

The ingestion layer utilizes stream-based message buffering:

- **Edge Gateway**: Authenticates incoming payloads via mTLS.
- **Worker Pools**: Auto-scaling consumer group dynamically spawned across node pools.
- **Format Normalizer**: Converts arbitrary binaries (PDF, DOCX, XLSX, HTML) into standardized Markdown AST tokens.

```kotlin
data class IngestionTask(
    val taskId: UUID,
    val documentUri: URI,
    val mimeType: String,
    val priority: PriorityLevel = PriorityLevel.STANDARD
) {
    fun execute(engine: MarkItDownEngine): ConvertedResult {
        return engine.process(documentUri)
    }
}
```

## 3. Data Flow Diagram
> **Pipeline Stages**: Client Request -> API Gateway -> RabbitMQ Broker -> Worker Node -> AST Parser -> Vector Embedder -> Vector DB

### Security Considerations
1. All network endpoints enforce TLS 1.3 encryption.
2. In-flight token streams are cleared from memory buffers after completion.
3. Access tokens expire after 15 minutes of inactivity.
                """.trimIndent(),
                wordCount = 230,
                charCount = 1520,
                readingTimeMinutes = 2,
                timestamp = System.currentTimeMillis() - 86400000,
                isFavorite = true,
                tags = "tech, architecture, word"
            ),

            ConversionDocument(
                id = -3,
                title = "Product Launch Pitch Deck",
                sourceFileName = "product_pitch_deck.pptx",
                sourceFormat = "PPTX",
                fileSizeBytes = 2450000,
                markdownContent = """
# Presentation Slides

## Slide 1: MarkItDown: Universal Document Intelligence
- Transform any unstructured document into clean, LLM-ready Markdown
- Fast, local-first conversion with zero cloud dependency
- Built for Android & Edge devices

## Slide 2: The Problem
- Enterprise data is trapped in silos: PDFs, Word documents, legacy spreadsheets
- Inconsistent parsing breaks retrieval-augmented generation (RAG) pipelines
- Existing tools are heavyweight or require expensive cloud subscriptions

## Slide 3: The Solution
- **One unified engine** for 10+ file formats
- Clean Markdown with native tables, codeblocks, and math formatting
- Instant local processing + optional Gemini multimodal AI enhancement

## Slide 4: Market Opportunity
- 80% of enterprise knowledge exists in unstructured document formats
- 300M+ developers and knowledge workers building AI-driven workflows
- $14.2B estimated market size for document ingestion & semantic search

## Slide 5: Roadmap & Next Milestones
- **Q4**: Real-time camera OCR integration
- **Q1**: Multi-document cross-referencing & auto-tagging
- **Q2**: Enterprise private cloud sync
                """.trimIndent(),
                wordCount = 175,
                charCount = 1180,
                readingTimeMinutes = 1,
                timestamp = System.currentTimeMillis() - 3600000 * 4,
                isFavorite = false,
                tags = "presentation, pitch, slides"
            ),

            ConversionDocument(
                id = -4,
                title = "E-Commerce Customer & Order API",
                sourceFileName = "api_customers_export.json",
                sourceFormat = "JSON",
                fileSizeBytes = 34500,
                markdownContent = """
# JSON Document

### Document Keys
- **`apiVersion`**: String
- **`totalRecords`**: Number (1,248)
- **`customers`**: Array (20 items previewed)

### Summary Table

| id | name | email | tier | totalSpent | status |
| --- | --- | --- | --- | --- | --- |
| CUST-101 | Elena Rostova | elena@example.com | Platinum | $12,450.00 | Active |
| CUST-102 | Marcus Chen | marcus@example.com | Gold | $6,320.50 | Active |
| CUST-103 | Sophia Bennett | sophia@example.com | Silver | $2,180.00 | Active |
| CUST-104 | David Okafor | david@example.com | Bronze | $750.00 | Pending |
| CUST-105 | Lisa Lindqvist | lisa@example.com | Platinum | $18,900.00 | Active |

### Formatted JSON

```json
{
  "apiVersion": "v2.1",
  "status": "success",
  "data": {
    "totalRecords": 1248,
    "pageSize": 50,
    "page": 1,
    "customers": [
      {
        "id": "CUST-101",
        "name": "Elena Rostova",
        "email": "elena@example.com",
        "tier": "Platinum",
        "totalSpent": 12450.0,
        "active": true
      }
    ]
  }
}
```
                """.trimIndent(),
                wordCount = 140,
                charCount = 980,
                readingTimeMinutes = 1,
                timestamp = System.currentTimeMillis() - 3600000 * 12,
                isFavorite = false,
                tags = "api, json, data"
            ),

            ConversionDocument(
                id = -5,
                title = "Modern Web Development Best Practices",
                sourceFileName = "web_development_guide.html",
                sourceFormat = "HTML",
                fileSizeBytes = 18600,
                markdownContent = """
# Modern Web Development: Architecture & Performance in 2026

Modern web applications demand lightning-fast initial load times, resilient offline capabilities, and seamless user experiences across devices.

## Core Architectural Pillars

### 1. Zero-Bundle-Size Server Components
By rendering stateful views on the edge and streaming only necessary dynamic hydrations, client payloads drop by upwards of **65%**.

> "Performance is not a feature; it is an architectural prerequisite."

### 2. Edge-First Data Fetching
Deploying data pipelines geographically adjacent to end users minimizes round-trip latency:

```javascript
export async function getDocumentIndex({ region, namespace }) {
  const edgeStore = await initEdgeConnection(region);
  return await edgeStore.query({ namespace, limit: 100 });
}
```

### Performance Benchmarks

| Metric | Target (P95) | Prior Standard | Improvement |
| --- | --- | --- | --- |
| TTFB (Time to First Byte) | < 120ms | 450ms | 3.75x faster |
| LCP (Largest Contentful Paint) | < 1.2s | 2.8s | 2.33x faster |
| INP (Interaction to Next Paint) | < 80ms | 220ms | 2.75x faster |

For further details, refer to the [Web Performance Standards](https://web.dev) guidelines.
                """.trimIndent(),
                wordCount = 180,
                charCount = 1250,
                readingTimeMinutes = 1,
                timestamp = System.currentTimeMillis() - 3600000 * 36,
                isFavorite = true,
                tags = "web, article, html"
            )
        )
    }
}
